/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.mc_cubed.icedjava.ice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author charles
 */
public class PairStatusTableModel extends AbstractTableModel {

    public Map<IceSocket, List<CandidatePair>> pairs;
    public List<CandidatePair> pairList = new ArrayList<CandidatePair>();

    protected void rebuildList() {
        if (pairs != null) {
            pairList.clear();
            for (List<CandidatePair> addPairs : pairs.values()) {
                pairList.addAll(addPairs);
            }
        }
        Collections.sort(pairList,new CandidatePairComparison());
    }

    public PairStatusTableModel(List<CandidatePair> pairList) {
        this.pairList = pairList;
    }

    public PairStatusTableModel(Map<IceSocket,List<CandidatePair>> pairs) {
        this.pairs = pairs;
    }

    @Override
    public int getRowCount() {
        rebuildList();
        return pairList.size();
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Type";
            case 1:
                return "Socket";
            case 2:
                return "Local";
            case 3:
                return "Remote";
            case 4:
                return "Component";
            case 5:
                return "Priority";
            case 6:
                return "Status";
            default:
                return "";
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        CandidatePair pair = pairList.get(row);
        switch (column) {
            case 0:
                return pair.getRemoteCandidate().getType();
            case 1:
                return Integer.toHexString(pair.getLocalCandidate().getIceSocket().hashCode());
            case 2:
                return pair.getLocalCandidate().getSocketAddress();
            case 3:
                return pair.getRemoteCandidate().getSocketAddress();
            case 4:
                return pair.getComponentId();
            case 5:
                return Long.toHexString(pair.getPriority());
            case 6:
                return pair.getState();
            default:
                return "";
        }
    }
}
