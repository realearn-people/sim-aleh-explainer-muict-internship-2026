package sim.explainer.library.framework.reasoner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sim.explainer.library.framework.PreferenceProfile;
import sim.explainer.library.framework.descriptiontree.BreadthFirstTreeIterator;
import sim.explainer.library.framework.descriptiontree.Tree;
import sim.explainer.library.framework.descriptiontree.TreeNode;
import sim.explainer.library.framework.explainer.BacktraceTable;
import sim.explainer.library.framework.explainer.SimRecord;
import sim.explainer.library.framework.unfolding.IRoleUnfolder;
import sim.explainer.library.framework.unfolding.ISubRoleUnfolder;

public class DynamicALEHSimPiReasonerImpl extends TopDownALEHSimPiReasonerImpl {

    private Map<Integer, Map<Integer, BigDecimal>> nodePairHdValMap;

    public DynamicALEHSimPiReasonerImpl(PreferenceProfile preferenceProfile,
                                         IRoleUnfolder iRoleUnfolder,
                                         ISubRoleUnfolder iSubRoleUnfolder) {
        super(preferenceProfile, iRoleUnfolder, iSubRoleUnfolder);
        this.nodePairHdValMap = new HashMap<>();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Private /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void addNodePairHdValMap(int node1Id, int node2Id, BigDecimal val) {
        nodePairHdValMap.computeIfAbsent(node1Id, k -> new HashMap<>()).put(node2Id, val);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Protected /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected BigDecimal eHdPi(int level, SimRecord record,
                              TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        String r = node1.getEdgeToParent();
        String s = node2.getEdgeToParent();
        BigDecimal gamma = eGamma(r, s);
        BigDecimal d = dHat(r);

        BigDecimal subTree = ZERO;
        Map<Integer, BigDecimal> innerMap = nodePairHdValMap.get(node1.getId());
        if (innerMap != null && innerMap.containsKey(node2.getId())) {
            subTree = innerMap.get(node2.getId());
        }

        return gamma.multiply(d.add(ONE.subtract(d).multiply(subTree)));
    }

    @Override
    protected BigDecimal aHdPi(int level, SimRecord record,
                              TreeNode<Set<String>> node1, TreeNode<Set<String>> node2) {
        String r = node1.getEdgeToParent();
        String s = node2.getEdgeToParent();
        BigDecimal gamma = aGamma(r, s);

        if (node2.getData().contains("⊥") || node2.getData().contains("BOTTOM")) {
            return gamma;
        }

        BigDecimal d = dHat(r);
        BigDecimal subTree = ZERO;
        Map<Integer, BigDecimal> innerMap = nodePairHdValMap.get(node1.getId());
        if (innerMap != null && innerMap.containsKey(node2.getId())) {
            subTree = innerMap.get(node2.getId());
        }

        return gamma.multiply(d.add(ONE.subtract(d).multiply(subTree)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Public //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public BigDecimal measureDirectedSimilarity(Tree<Set<String>> tree1, Tree<Set<String>> tree2) {
        this.nodePairHdValMap = new HashMap<>();
        this.backtraceTable = new BacktraceTable();

        BreadthFirstTreeIterator<Set<String>> iter1 = (BreadthFirstTreeIterator<Set<String>>) tree1.iterator(0);
        BreadthFirstTreeIterator<Set<String>> iter2 = (BreadthFirstTreeIterator<Set<String>>) tree2.iterator(0);

        int height = iter1.getNodesOnEachLevel().size();

        for (int i = height - 1; i >= 0; i--) {
            List<TreeNode<Set<String>>> list1 = iter1.getNodesOnEachLevel().get(i);
            List<TreeNode<Set<String>>> list2 = iter2.getNodesOnEachLevel().get(i);

            if (list1 == null || list2 == null) continue;

            for (TreeNode<Set<String>> treeNode1 : list1) {
                for (TreeNode<Set<String>> treeNode2 : list2) {
                    SimRecord record = new SimRecord();
                    BigDecimal hdVal;

                    if (i == height - 1) {
                        hdVal = pHdPi(record, treeNode1, treeNode2);
                    } else {
                        BigDecimal muEVal = muE(treeNode1);
                        BigDecimal muAVal = muA(treeNode1);
                        BigDecimal muPVal = BigDecimal.ONE.subtract(muEVal).subtract(muAVal);
                        hdVal = muPVal.multiply(pHdPi(record, treeNode1, treeNode2))
                                .add(muEVal.multiply(eSetHdPi(i, record, treeNode1, treeNode2)))
                                .add(muAVal.multiply(aSetHdPi(i, record, treeNode1, treeNode2)));
                    }

                    record.setDeg(hdVal);
                    addNodePairHdValMap(treeNode1.getId(), treeNode2.getId(), hdVal);
                    this.backtraceTable.addRecord(i, treeNode1, treeNode2, record);
                }
            }
        }

        return nodePairHdValMap.getOrDefault(0, new HashMap<>()).getOrDefault(0, BigDecimal.ZERO);
    }

}