/*
 * Decompiled with CFR 0.152.
 */
package org.jbox2d.collision.broadphase;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.TreeCallback;
import org.jbox2d.callbacks.TreeRayCastCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.RayCastInput;
import org.jbox2d.collision.broadphase.BroadPhaseStrategy;
import org.jbox2d.collision.broadphase.DynamicTreeNode;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

public class DynamicTree
implements BroadPhaseStrategy {
    public static final int MAX_STACK_SIZE = 64;
    public static final int NULL_NODE = -1;
    private DynamicTreeNode m_root = null;
    private DynamicTreeNode[] m_nodes;
    private int m_nodeCount = 0;
    private int m_nodeCapacity = 16;
    private int m_freeList;
    private int m_insertionCount;
    private final Vec2[] drawVecs = new Vec2[4];
    private final TreeNodeStack nodeStack = new TreeNodeStack(10);
    private final Vec2 r = new Vec2();
    private final AABB aabb = new AABB();
    private final RayCastInput subInput = new RayCastInput();
    private final AABB combinedAABB = new AABB();
    private final Color3f color = new Color3f();
    private final Vec2 textVec = new Vec2();

    public DynamicTree() {
        int i;
        this.m_nodes = new DynamicTreeNode[16];
        for (i = this.m_nodeCapacity - 1; i >= 0; --i) {
            this.m_nodes[i] = new DynamicTreeNode(i);
            this.m_nodes[i].parent = i == this.m_nodeCapacity - 1 ? null : this.m_nodes[i + 1];
            this.m_nodes[i].height = -1;
        }
        this.m_freeList = 0;
        this.m_insertionCount = 0;
        for (i = 0; i < this.drawVecs.length; ++i) {
            this.drawVecs[i] = new Vec2();
        }
    }

    @Override
    public final int createProxy(AABB aabb, Object userData) {
        DynamicTreeNode node = this.allocateNode();
        int proxyId = node.id;
        AABB nodeAABB = node.aabb;
        nodeAABB.lowerBound.x = aabb.lowerBound.x - 0.1f;
        nodeAABB.lowerBound.y = aabb.lowerBound.y - 0.1f;
        nodeAABB.upperBound.x = aabb.upperBound.x + 0.1f;
        nodeAABB.upperBound.y = aabb.upperBound.y + 0.1f;
        node.userData = userData;
        this.insertLeaf(proxyId);
        return proxyId;
    }

    @Override
    public final void destroyProxy(int proxyId) {
        assert (0 <= proxyId && proxyId < this.m_nodeCapacity);
        DynamicTreeNode node = this.m_nodes[proxyId];
        assert (node.isLeaf());
        this.removeLeaf(node);
        this.freeNode(node);
    }

    @Override
    public final boolean moveProxy(int proxyId, AABB aabb, Vec2 displacement) {
        assert (0 <= proxyId && proxyId < this.m_nodeCapacity);
        DynamicTreeNode node = this.m_nodes[proxyId];
        assert (node.isLeaf());
        AABB nodeAABB = node.aabb;
        if (nodeAABB.lowerBound.x > aabb.lowerBound.x && nodeAABB.lowerBound.y > aabb.lowerBound.y && aabb.upperBound.x > nodeAABB.upperBound.x && aabb.upperBound.y > nodeAABB.upperBound.y) {
            return false;
        }
        this.removeLeaf(node);
        Vec2 lowerBound = nodeAABB.lowerBound;
        Vec2 upperBound = nodeAABB.upperBound;
        lowerBound.x = aabb.lowerBound.x - 0.1f;
        lowerBound.y = aabb.lowerBound.y - 0.1f;
        upperBound.x = aabb.upperBound.x + 0.1f;
        upperBound.y = aabb.upperBound.y + 0.1f;
        float dx = displacement.x * 2.0f;
        float dy = displacement.y * 2.0f;
        if (dx < 0.0f) {
            lowerBound.x += dx;
        } else {
            upperBound.x += dx;
        }
        if (dy < 0.0f) {
            lowerBound.y += dy;
        } else {
            upperBound.y += dy;
        }
        this.insertLeaf(proxyId);
        return true;
    }

    @Override
    public final Object getUserData(int proxyId) {
        assert (0 <= proxyId && proxyId < this.m_nodeCapacity);
        return this.m_nodes[proxyId].userData;
    }

    @Override
    public final AABB getFatAABB(int proxyId) {
        assert (0 <= proxyId && proxyId < this.m_nodeCapacity);
        return this.m_nodes[proxyId].aabb;
    }

    @Override
    public final void query(TreeCallback callback, AABB aabb) {
        this.nodeStack.reset();
        this.nodeStack.push(this.m_root);
        while (this.nodeStack.getCount() > 0) {
            DynamicTreeNode node = this.nodeStack.pop();
            if (node == null || !AABB.testOverlap(node.aabb, aabb)) continue;
            if (node.child1 == null) {
                boolean proceed = callback.treeCallback(node.id);
                if (proceed) continue;
                return;
            }
            this.nodeStack.push(node.child1);
            this.nodeStack.push(node.child2);
        }
    }

    @Override
    public void raycast(TreeRayCastCallback callback, RayCastInput input) {
        Vec2 p1 = input.p1;
        Vec2 p2 = input.p2;
        float p1x = p1.x;
        float p2x = p2.x;
        float p1y = p1.y;
        float p2y = p2.y;
        this.r.x = p2x - p1x;
        this.r.y = p2y - p1y;
        assert (this.r.x * this.r.x + this.r.y * this.r.y > 0.0f);
        this.r.normalize();
        float rx = this.r.x;
        float ry = this.r.y;
        float vx = -1.0f * ry;
        float vy = 1.0f * rx;
        float absVx = MathUtils.abs(vx);
        float absVy = MathUtils.abs(vy);
        float maxFraction = input.maxFraction;
        AABB segAABB = this.aabb;
        float tempx = (p2x - p1x) * maxFraction + p1x;
        float tempy = (p2y - p1y) * maxFraction + p1y;
        segAABB.lowerBound.x = p1x < tempx ? p1x : tempx;
        segAABB.lowerBound.y = p1y < tempy ? p1y : tempy;
        segAABB.upperBound.x = p1x > tempx ? p1x : tempx;
        segAABB.upperBound.y = p1y > tempy ? p1y : tempy;
        this.nodeStack.reset();
        this.nodeStack.push(this.m_root);
        while (this.nodeStack.getCount() > 0) {
            AABB nodeAABB;
            DynamicTreeNode node = this.nodeStack.pop();
            if (node == null || !AABB.testOverlap(nodeAABB = node.aabb, segAABB)) continue;
            float cx = (nodeAABB.lowerBound.x + nodeAABB.upperBound.x) * 0.5f;
            float cy = (nodeAABB.lowerBound.y + nodeAABB.upperBound.y) * 0.5f;
            float hx = (nodeAABB.upperBound.x - nodeAABB.lowerBound.x) * 0.5f;
            float hy = (nodeAABB.upperBound.y - nodeAABB.lowerBound.y) * 0.5f;
            tempx = p1x - cx;
            tempy = p1y - cy;
            float separation = MathUtils.abs(vx * tempx + vy * tempy) - (absVx * hx + absVy * hy);
            if (separation > 0.0f) continue;
            if (node.isLeaf()) {
                this.subInput.p1.x = p1x;
                this.subInput.p1.y = p1y;
                this.subInput.p2.x = p2x;
                this.subInput.p2.y = p2y;
                this.subInput.maxFraction = maxFraction;
                float value = callback.raycastCallback(this.subInput, node.id);
                if (value == 0.0f) {
                    return;
                }
                if (!(value > 0.0f)) continue;
                maxFraction = value;
                tempx = (p2x - p1x) * maxFraction + p1x;
                tempy = (p2y - p1y) * maxFraction + p1y;
                segAABB.lowerBound.x = p1x < tempx ? p1x : tempx;
                segAABB.lowerBound.y = p1y < tempy ? p1y : tempy;
                segAABB.upperBound.x = p1x > tempx ? p1x : tempx;
                segAABB.upperBound.y = p1y > tempy ? p1y : tempy;
                continue;
            }
            this.nodeStack.push(node.child1);
            this.nodeStack.push(node.child2);
        }
    }

    @Override
    public final int computeHeight() {
        return this.computeHeight(this.m_root);
    }

    private final int computeHeight(DynamicTreeNode node) {
        assert (0 <= node.id && node.id < this.m_nodeCapacity);
        if (node.isLeaf()) {
            return 0;
        }
        int height1 = this.computeHeight(node.child1);
        int height2 = this.computeHeight(node.child2);
        return 1 + MathUtils.max(height1, height2);
    }

    public void validate() {
        DynamicTreeNode freeNode;
        this.validateStructure(this.m_root);
        this.validateMetrics(this.m_root);
        int freeCount = 0;
        DynamicTreeNode dynamicTreeNode = freeNode = this.m_freeList != -1 ? this.m_nodes[this.m_freeList] : null;
        while (freeNode != null) {
            assert (0 <= freeNode.id && freeNode.id < this.m_nodeCapacity);
            assert (freeNode == this.m_nodes[freeNode.id]);
            freeNode = freeNode.parent;
            ++freeCount;
        }
        assert (this.getHeight() == this.computeHeight());
        assert (this.m_nodeCount + freeCount == this.m_nodeCapacity);
    }

    @Override
    public int getHeight() {
        if (this.m_root == null) {
            return 0;
        }
        return this.m_root.height;
    }

    @Override
    public int getMaxBalance() {
        int maxBalance = 0;
        for (int i = 0; i < this.m_nodeCapacity; ++i) {
            DynamicTreeNode node = this.m_nodes[i];
            if (node.height <= 1) continue;
            assert (!node.isLeaf());
            DynamicTreeNode child1 = node.child1;
            DynamicTreeNode child2 = node.child2;
            int balance = MathUtils.abs(child2.height - child1.height);
            maxBalance = MathUtils.max(maxBalance, balance);
        }
        return maxBalance;
    }

    @Override
    public float getAreaRatio() {
        if (this.m_root == null) {
            return 0.0f;
        }
        DynamicTreeNode root = this.m_root;
        float rootArea = root.aabb.getPerimeter();
        float totalArea = 0.0f;
        for (int i = 0; i < this.m_nodeCapacity; ++i) {
            DynamicTreeNode node = this.m_nodes[i];
            if (node.height < 0) continue;
            totalArea += node.aabb.getPerimeter();
        }
        return totalArea / rootArea;
    }

    public void rebuildBottomUp() {
        int[] nodes = new int[this.m_nodeCount];
        int count = 0;
        for (int i = 0; i < this.m_nodeCapacity; ++i) {
            if (this.m_nodes[i].height < 0) continue;
            DynamicTreeNode node = this.m_nodes[i];
            if (node.isLeaf()) {
                node.parent = null;
                nodes[count] = i;
                ++count;
                continue;
            }
            this.freeNode(node);
        }
        AABB b = new AABB();
        while (count > 1) {
            float minCost = Float.MAX_VALUE;
            int iMin = -1;
            int jMin = -1;
            for (int i = 0; i < count; ++i) {
                AABB aabbi = this.m_nodes[nodes[i]].aabb;
                for (int j = i + 1; j < count; ++j) {
                    AABB aabbj = this.m_nodes[nodes[j]].aabb;
                    b.combine(aabbi, aabbj);
                    float cost = b.getPerimeter();
                    if (!(cost < minCost)) continue;
                    iMin = i;
                    jMin = j;
                    minCost = cost;
                }
            }
            int index1 = nodes[iMin];
            int index2 = nodes[jMin];
            DynamicTreeNode child1 = this.m_nodes[index1];
            DynamicTreeNode child2 = this.m_nodes[index2];
            DynamicTreeNode parent = this.allocateNode();
            parent.child1 = child1;
            parent.child2 = child2;
            parent.height = 1 + MathUtils.max(child1.height, child2.height);
            parent.aabb.combine(child1.aabb, child2.aabb);
            parent.parent = null;
            child1.parent = parent;
            child2.parent = parent;
            nodes[jMin] = nodes[count - 1];
            nodes[iMin] = parent.id;
            --count;
        }
        this.m_root = this.m_nodes[nodes[0]];
        this.validate();
    }

    private final DynamicTreeNode allocateNode() {
        if (this.m_freeList == -1) {
            assert (this.m_nodeCount == this.m_nodeCapacity);
            DynamicTreeNode[] old = this.m_nodes;
            this.m_nodeCapacity *= 2;
            this.m_nodes = new DynamicTreeNode[this.m_nodeCapacity];
            System.arraycopy(old, 0, this.m_nodes, 0, old.length);
            for (int i = this.m_nodeCapacity - 1; i >= this.m_nodeCount; --i) {
                this.m_nodes[i] = new DynamicTreeNode(i);
                this.m_nodes[i].parent = i == this.m_nodeCapacity - 1 ? null : this.m_nodes[i + 1];
                this.m_nodes[i].height = -1;
            }
            this.m_freeList = this.m_nodeCount;
        }
        int nodeId = this.m_freeList;
        DynamicTreeNode treeNode = this.m_nodes[nodeId];
        this.m_freeList = treeNode.parent != null ? treeNode.parent.id : -1;
        treeNode.parent = null;
        treeNode.child1 = null;
        treeNode.child2 = null;
        treeNode.height = 0;
        treeNode.userData = null;
        ++this.m_nodeCount;
        return treeNode;
    }

    private final void freeNode(DynamicTreeNode node) {
        assert (node != null);
        assert (0 < this.m_nodeCount);
        node.parent = this.m_freeList != -1 ? this.m_nodes[this.m_freeList] : null;
        node.height = -1;
        this.m_freeList = node.id;
        --this.m_nodeCount;
    }

    @Override
    public int getInsertionCount() {
        return this.m_insertionCount;
    }

    private final void insertLeaf(int leaf_index) {
        ++this.m_insertionCount;
        DynamicTreeNode leaf = this.m_nodes[leaf_index];
        if (this.m_root == null) {
            this.m_root = leaf;
            this.m_root.parent = null;
            return;
        }
        AABB leafAABB = leaf.aabb;
        DynamicTreeNode index = this.m_root;
        while (index.child1 != null) {
            float cost2;
            float cost1;
            DynamicTreeNode node = index;
            DynamicTreeNode child1 = node.child1;
            DynamicTreeNode child2 = node.child2;
            float area = node.aabb.getPerimeter();
            this.combinedAABB.combine(node.aabb, leafAABB);
            float combinedArea = this.combinedAABB.getPerimeter();
            float cost = 2.0f * combinedArea;
            float inheritanceCost = 2.0f * (combinedArea - area);
            if (child1.isLeaf()) {
                this.combinedAABB.combine(leafAABB, child1.aabb);
                cost1 = this.combinedAABB.getPerimeter() + inheritanceCost;
            } else {
                this.combinedAABB.combine(leafAABB, child1.aabb);
                float oldArea = child1.aabb.getPerimeter();
                float newArea = this.combinedAABB.getPerimeter();
                cost1 = newArea - oldArea + inheritanceCost;
            }
            if (child2.isLeaf()) {
                this.combinedAABB.combine(leafAABB, child2.aabb);
                cost2 = this.combinedAABB.getPerimeter() + inheritanceCost;
            } else {
                this.combinedAABB.combine(leafAABB, child2.aabb);
                float oldArea = child2.aabb.getPerimeter();
                float newArea = this.combinedAABB.getPerimeter();
                cost2 = newArea - oldArea + inheritanceCost;
            }
            if (cost < cost1 && cost < cost2) break;
            if (cost1 < cost2) {
                index = child1;
                continue;
            }
            index = child2;
        }
        DynamicTreeNode sibling = index;
        DynamicTreeNode oldParent = this.m_nodes[sibling.id].parent;
        DynamicTreeNode newParent = this.allocateNode();
        newParent.parent = oldParent;
        newParent.userData = null;
        newParent.aabb.combine(leafAABB, sibling.aabb);
        newParent.height = sibling.height + 1;
        if (oldParent != null) {
            if (oldParent.child1 == sibling) {
                oldParent.child1 = newParent;
            } else {
                oldParent.child2 = newParent;
            }
            newParent.child1 = sibling;
            newParent.child2 = leaf;
            sibling.parent = newParent;
            leaf.parent = newParent;
        } else {
            newParent.child1 = sibling;
            newParent.child2 = leaf;
            sibling.parent = newParent;
            leaf.parent = newParent;
            this.m_root = newParent;
        }
        index = leaf.parent;
        while (index != null) {
            index = this.balance(index);
            DynamicTreeNode child1 = index.child1;
            DynamicTreeNode child2 = index.child2;
            assert (child1 != null);
            assert (child2 != null);
            index.height = 1 + MathUtils.max(child1.height, child2.height);
            index.aabb.combine(child1.aabb, child2.aabb);
            index = index.parent;
        }
    }

    private final void removeLeaf(DynamicTreeNode leaf) {
        if (leaf == this.m_root) {
            this.m_root = null;
            return;
        }
        DynamicTreeNode parent = leaf.parent;
        DynamicTreeNode grandParent = parent.parent;
        DynamicTreeNode sibling = parent.child1 == leaf ? parent.child2 : parent.child1;
        if (grandParent != null) {
            if (grandParent.child1 == parent) {
                grandParent.child1 = sibling;
            } else {
                grandParent.child2 = sibling;
            }
            sibling.parent = grandParent;
            this.freeNode(parent);
            DynamicTreeNode index = grandParent;
            while (index != null) {
                index = this.balance(index);
                DynamicTreeNode child1 = index.child1;
                DynamicTreeNode child2 = index.child2;
                index.aabb.combine(child1.aabb, child2.aabb);
                index.height = 1 + MathUtils.max(child1.height, child2.height);
                index = index.parent;
            }
        } else {
            this.m_root = sibling;
            sibling.parent = null;
            this.freeNode(parent);
        }
    }

    private DynamicTreeNode balance(DynamicTreeNode iA) {
        assert (iA != null);
        DynamicTreeNode A = iA;
        if (A.isLeaf() || A.height < 2) {
            return iA;
        }
        DynamicTreeNode iB = A.child1;
        DynamicTreeNode iC = A.child2;
        assert (0 <= iB.id && iB.id < this.m_nodeCapacity);
        assert (0 <= iC.id && iC.id < this.m_nodeCapacity);
        DynamicTreeNode B = iB;
        DynamicTreeNode C = iC;
        int balance = C.height - B.height;
        if (balance > 1) {
            DynamicTreeNode iF = C.child1;
            DynamicTreeNode iG = C.child2;
            DynamicTreeNode F = iF;
            DynamicTreeNode G = iG;
            assert (F != null);
            assert (G != null);
            assert (0 <= iF.id && iF.id < this.m_nodeCapacity);
            assert (0 <= iG.id && iG.id < this.m_nodeCapacity);
            C.child1 = iA;
            C.parent = A.parent;
            A.parent = iC;
            if (C.parent != null) {
                if (C.parent.child1 == iA) {
                    C.parent.child1 = iC;
                } else {
                    assert (C.parent.child2 == iA);
                    C.parent.child2 = iC;
                }
            } else {
                this.m_root = iC;
            }
            if (F.height > G.height) {
                C.child2 = iF;
                A.child2 = iG;
                G.parent = iA;
                A.aabb.combine(B.aabb, G.aabb);
                C.aabb.combine(A.aabb, F.aabb);
                A.height = 1 + MathUtils.max(B.height, G.height);
                C.height = 1 + MathUtils.max(A.height, F.height);
            } else {
                C.child2 = iG;
                A.child2 = iF;
                F.parent = iA;
                A.aabb.combine(B.aabb, F.aabb);
                C.aabb.combine(A.aabb, G.aabb);
                A.height = 1 + MathUtils.max(B.height, F.height);
                C.height = 1 + MathUtils.max(A.height, G.height);
            }
            return iC;
        }
        if (balance < -1) {
            DynamicTreeNode iD = B.child1;
            DynamicTreeNode iE = B.child2;
            DynamicTreeNode D = iD;
            DynamicTreeNode E = iE;
            assert (0 <= iD.id && iD.id < this.m_nodeCapacity);
            assert (0 <= iE.id && iE.id < this.m_nodeCapacity);
            B.child1 = iA;
            B.parent = A.parent;
            A.parent = iB;
            if (B.parent != null) {
                if (B.parent.child1 == iA) {
                    B.parent.child1 = iB;
                } else {
                    assert (B.parent.child2 == iA);
                    B.parent.child2 = iB;
                }
            } else {
                this.m_root = iB;
            }
            if (D.height > E.height) {
                B.child2 = iD;
                A.child1 = iE;
                E.parent = iA;
                A.aabb.combine(C.aabb, E.aabb);
                B.aabb.combine(A.aabb, D.aabb);
                A.height = 1 + MathUtils.max(C.height, E.height);
                B.height = 1 + MathUtils.max(A.height, D.height);
            } else {
                B.child2 = iE;
                A.child1 = iD;
                D.parent = iA;
                A.aabb.combine(C.aabb, D.aabb);
                B.aabb.combine(A.aabb, E.aabb);
                A.height = 1 + MathUtils.max(C.height, D.height);
                B.height = 1 + MathUtils.max(A.height, E.height);
            }
            return iB;
        }
        return iA;
    }

    private void validateStructure(DynamicTreeNode node) {
        if (node == null) {
            return;
        }
        assert (node == this.m_nodes[node.id]);
        if (node == this.m_root) assert (node.parent == null);
        DynamicTreeNode child1 = node.child1;
        DynamicTreeNode child2 = node.child2;
        if (node.isLeaf()) {
            assert (child1 == null);
            assert (child2 == null);
            assert (node.height == 0);
            return;
        }
        assert (child1 != null && 0 <= child1.id && child1.id < this.m_nodeCapacity);
        assert (child2 != null && 0 <= child2.id && child2.id < this.m_nodeCapacity);
        assert (child1.parent == node);
        assert (child2.parent == node);
        this.validateStructure(child1);
        this.validateStructure(child2);
    }

    private void validateMetrics(DynamicTreeNode node) {
        if (node == null) {
            return;
        }
        DynamicTreeNode child1 = node.child1;
        DynamicTreeNode child2 = node.child2;
        if (node.isLeaf()) {
            assert (child1 == null);
            assert (child2 == null);
            assert (node.height == 0);
            return;
        }
        assert (child1 != null && 0 <= child1.id && child1.id < this.m_nodeCapacity);
        assert (child2 != null && 0 <= child2.id && child2.id < this.m_nodeCapacity);
        int height1 = child1.height;
        int height2 = child2.height;
        int height = 1 + MathUtils.max(height1, height2);
        assert (node.height == height);
        AABB aabb = new AABB();
        aabb.combine(child1.aabb, child2.aabb);
        assert (aabb.lowerBound.equals(node.aabb.lowerBound));
        assert (aabb.upperBound.equals(node.aabb.upperBound));
        this.validateMetrics(child1);
        this.validateMetrics(child2);
    }

    @Override
    public void drawTree(DebugDraw argDraw) {
        if (this.m_root == null) {
            return;
        }
        int height = this.computeHeight();
        this.drawTree(argDraw, this.m_root, 0, height);
    }

    public void drawTree(DebugDraw argDraw, DynamicTreeNode node, int spot, int height) {
        node.aabb.getVertices(this.drawVecs);
        this.color.set(1.0f, (float)(height - spot) * 1.0f / (float)height, (float)(height - spot) * 1.0f / (float)height);
        argDraw.drawPolygon(this.drawVecs, 4, this.color);
        argDraw.getViewportTranform().getWorldToScreen(node.aabb.upperBound, this.textVec);
        argDraw.drawString(this.textVec.x, this.textVec.y, node.id + "-" + (spot + 1) + "/" + height, this.color);
        if (node.child1 != null) {
            this.drawTree(argDraw, node.child1, spot + 1, height);
        }
        if (node.child2 != null) {
            this.drawTree(argDraw, node.child2, spot + 1, height);
        }
    }

    public class TreeNodeStack {
        private DynamicTreeNode[] stack;
        private int size;
        private int position;

        public TreeNodeStack(int initialSize) {
            this.stack = new DynamicTreeNode[initialSize];
            this.position = 0;
            this.size = initialSize;
        }

        public void reset() {
            this.position = 0;
        }

        public DynamicTreeNode pop() {
            assert (this.position > 0);
            return this.stack[--this.position];
        }

        public void push(DynamicTreeNode i) {
            if (this.position == this.size) {
                DynamicTreeNode[] old = this.stack;
                this.stack = new DynamicTreeNode[this.size * 2];
                this.size = this.stack.length;
                System.arraycopy(old, 0, this.stack, 0, old.length);
            }
            this.stack[this.position++] = i;
        }

        public int getCount() {
            return this.position;
        }
    }
}

