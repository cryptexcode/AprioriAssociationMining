/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package apriori;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sudipta Kar <cryptexcode@gmail.com>
 */
public class MineItemsets {
    private ArrayList<ArrayList<Integer>> transactions_set;
    private HashMap<Integer, String> intToStrItem;
    private HashMap<String, Integer> strItemToInt;
    
    private HashMap<Integer, Integer> itemListMap;
    private HashMap<BitSet, Integer> itemListMap2;
    private List<Map.Entry<Integer, Integer>> frequentItemListSetLenOne; // Sorted list of set(item->support)
    private HashMap<Integer, Integer> itemIndex;    // Tracks index of item in itemList
    private int bitsetLen;
    private ArrayList<BitSet> bitTransactionList;
    
    private final int min_sup;
    private final int min_itemset_size_K;
    private final String inputFilePath;
    private final String outputFilePath;
    private HashMap<Set<Integer>, Integer> associations;

    public MineItemsets(int min_sup, int k, String inputPath, String outputPath) {
        this.min_sup = min_sup;
        this.min_itemset_size_K = k;
        this.inputFilePath = inputPath;
        this.outputFilePath = outputPath;
        this.associations = new HashMap<>();
        
        // Format the database
        loadAndProcessInput();
        createBitStremMap();
        convertTransactionToBitStream();
        apriori();
    }
    
    /**
     * This method reads the input transaction file and does the following.
     * 1. For each transaction, it creates two dictionary entries in item->ID
     * and ID->item hash maps. Then convert the items to integer id.
     * 2. Each transaction of items becomes a set of integers.
     * 3. After executing this method, we will get a transformed list of
     * transactions, two dictionary of item <-> ID
     */
    private void loadAndProcessInput(){
        transactions_set = new ArrayList<>();
        strItemToInt = new HashMap<>();
        intToStrItem = new HashMap<>();
        
        try{
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(this.inputFilePath)
                    ));
            
            String line;
            while((line = br.readLine()) != null){
                String[] tokens = line.split(" ");
                ArrayList<Integer> itemsId = new ArrayList();
                
                for(String item : tokens){
                    if( ! strItemToInt.containsKey(item)){
                        strItemToInt.put(item, strItemToInt.size()+1);
                        intToStrItem.put(strItemToInt.size(), item);
                    }
                    itemsId.add(strItemToInt.get(item));
                }
                transactions_set.add(itemsId);
            }
        }
        catch(IOException fnfEx){
            System.err.println("Input Error");
            fnfEx.printStackTrace();
        }
    
    }
  
    
    /**
     * 1. Scans the transaction DB.
     * 2. Creates itemListMap (item -> support)
     * 3. Remove items not having minimum support
     * 4. Sort itemListMap to itemListSet (decreasing by value)
     * 5. Create itemIndex (item -> position in itemListSet)
     * 6. Set bitsetLen -> size of itemIndex
     */
    private void createBitStremMap(){
        itemListMap = new HashMap<>();
        
        for (ArrayList<Integer> transactions_set1 : transactions_set) {
            ArrayList curr_tr = (ArrayList) transactions_set1;
            if(curr_tr.size() >= min_itemset_size_K)    
                for (Object curr_tr1 : curr_tr) {
                    int item = (int) curr_tr1;
                itemListMap.put(item, itemListMap.getOrDefault(item, 0)+1);
            }
        }
        
        // 3. Remove items without minimum support
        itemListMap.values().removeIf(val -> val<min_sup);
        
        // 4. Sort the map to a set of entries
        Set<Map.Entry<Integer, Integer>> set = itemListMap.entrySet();
        frequentItemListSetLenOne = new ArrayList<>(set);
        Collections.sort( frequentItemListSetLenOne, new Comparator<Map.Entry<Integer, Integer>>(){
            @Override
            public int compare( Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        });

        // 5.
        itemIndex = new HashMap<>();
        for(Map.Entry<Integer, Integer> entry : frequentItemListSetLenOne){
            itemIndex.put(entry.getKey(), itemIndex.size());
        }

        // 6.
        bitsetLen = frequentItemListSetLenOne.size();
    }
    
    
    /**
     * For each transaction, make a BitSet of size bitsetLen.
     * Set 1 for present items.
     */
    private void convertTransactionToBitStream(){
        bitTransactionList = new ArrayList<>();
        itemListMap2 = new HashMap<>();
        
        for(int i=0; i<transactions_set.size(); i++){
            ArrayList curr_tr = transactions_set.get(i);
            
            if(curr_tr.size() >= min_itemset_size_K){ 
                int freqItem = 0;
                BitSet bitset = new BitSet(bitsetLen);
                
                for(int j=0; j<curr_tr.size(); j++){
                    try{
                        int item = (int)curr_tr.get(j);
                        bitset.set(itemIndex.get(item), true);
                        freqItem++;
                    }
                    catch(Exception e){}
                }
                if(freqItem >= min_itemset_size_K){
                    bitTransactionList.add(bitset);
//                    System.out.println(bitset);
                    HashSet<BitSet> subset_2 = createSubsetsOfLen2(bitset);
                    for(BitSet sub_b : subset_2){
                        itemListMap2.put(sub_b, itemListMap2.getOrDefault(sub_b, 0)+1);
                    }
                }
            }
        }
        
        itemListMap2.values().removeIf(val -> val<min_sup);
    }
    
    
    /**
     * 
     * @param frequentItemsPrevLevel
     * @return 
     */
    HashMap<BitSet, Integer> candidate_gen( HashMap<BitSet, Integer> frequentItemsPrevLevel, int len){
        HashMap<BitSet, Integer> initial_candidate_set = new HashMap<>();
        ArrayList<BitSet> frequentItems = new ArrayList<>();
        frequentItems.addAll(frequentItemsPrevLevel.keySet());
        
        // We already have the itemsets of length 2
        if(len == 2){
            initial_candidate_set = (HashMap<BitSet, Integer>)itemListMap2.clone();
        }
        else{
            for(int i=0; i<frequentItems.size()-1; i++){
                for(int j=i+1; j<frequentItems.size(); j++){
                    BitSet b1 = (BitSet)frequentItems.get(i).clone();
                    BitSet b2 = (BitSet)frequentItems.get(j).clone();
                    boolean canJoin = true;
                    
                    for(int bitIndex=0; bitIndex<len-2; bitIndex++){
                        canJoin = b1.nextSetBit(bitIndex) == b2.nextSetBit(bitIndex);
                        if(!canJoin)
                            break;
                    }

                    if(canJoin){
                        b1.or(frequentItems.get(j));
                        
                        if(b1.cardinality() == len){
                            // Check if subsets are frequent
                            HashSet<BitSet> subsets = createSubsets(b1);
                            boolean subsetsFrequent = true;
                            for(BitSet subset : subsets){
                                if(frequentItemsPrevLevel.getOrDefault(subset, -1) == -1){
                                    subsetsFrequent = false;
                                    break;
                                }
                            }
                            if(subsetsFrequent){
                                int support = 0;
                                for(BitSet transaction : bitTransactionList){
                                    if(transaction.cardinality() <len)
                                        continue;;
                                    BitSet andOp = (BitSet)transaction.clone();
                                    andOp.and(b1);
                                    if( andOp.equals(b1))
                                        support++;
                                }
                                if(support >= min_sup){
                                    initial_candidate_set.put(b1, support);
                                }
                                
                            }
                        }                            
                    }
                }
            }
        }
        
        return initial_candidate_set;
    }
    
    
    /**
     * This method runs the optimized apriori algorithm on the transaction
     * database and prints the associations in file by calling the 
     * printAssociations() method.
     */
    private void apriori(){
        HashMap<BitSet, Integer> frequentItemsBit = new HashMap<>();
        HashMap<BitSet, Integer> allAssociations = new HashMap<>();
        
        // Put all the 1-itemsets in frequent item hashmap. It will be scanned
        // for getting k-itemsets and cleared iteratively. 
        for (int i = 0; i < frequentItemListSetLenOne.size(); i++) {
            BitSet bitSet = new BitSet(bitsetLen);
            bitSet.set(i, true);
            frequentItemsBit.put(bitSet, frequentItemListSetLenOne.get(i).getValue());
        }

        int currentK = 1;
        boolean emptyFrequentSet = false;
        
        while (!emptyFrequentSet) {
            
            // create candidates
            if(currentK > 1){
                HashMap<BitSet, Integer> frequentItemsBitT = (HashMap)frequentItemsBit.clone();
                frequentItemsBit.clear();
                frequentItemsBit = candidate_gen(frequentItemsBitT, currentK);
            }
            
            // If no frequent itemSet in current level break
            if(frequentItemsBit.isEmpty())
                break;
            
            // if currentK >= minimumK insert into final list
            if (currentK >= min_itemset_size_K) {
                for (Map.Entry<BitSet, Integer> entry : frequentItemsBit.entrySet()) {
                    allAssociations.put(entry.getKey(), entry.getValue());
                }
            }
            currentK++;
        }
        
        printAssociations(allAssociations);
    }
    
    
    /**
     * This method creates a file in the outputPath.
     * Writes the associations with support.
     * @param allAssociations 
     */
    void printAssociations(HashMap<BitSet, Integer> allAssociations){
        try{
            boolean firstLine = true;
            FileWriter fw = new FileWriter(this.outputFilePath);
            
            for(Map.Entry<BitSet, Integer> entry : allAssociations.entrySet()){
                BitSet bs = entry.getKey();
                if(firstLine)
                    firstLine = false;
                else
                    fw.write("\n");
                
                for(int i=0; i<bs.length() ; i++){
                    if(bs.get(i))
                        fw.write(intToStrItem.get(frequentItemListSetLenOne.get(i).getKey())+" ");
                }

                fw.write("(" + entry.getValue() +")");
            }
            
            fw.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
   
    
    /**
     * This method prints the binary representation of a bitset.
     * @param bs
     * @return 
     */
    String bsToStr(BitSet bs){
        String s = "";
        for(int i=0; i<bitsetLen; i++){
            if(bs.get(i) == true)
                s += '1';
            else
                s += '0';
        }
        
        return new StringBuilder(s).reverse().toString();
    }
    
    
    /**
     * From a given transaction (set of id) of length n, it creates possible
     * subsets of n-1 size.
     * @param bs
     * @return 
     */
    HashSet<BitSet> createSubsets(BitSet bs){
        HashSet<BitSet> subsets = new HashSet<>();
        for(int i=0; i<bs.length(); i++){
            if(bs.get(i)){
                BitSet t = (BitSet)bs.clone();
                t.flip(i);
                subsets.add(t);
            }
        }
        
        return subsets;
    }
    
    
    /**
     * From a given set of transaction id, this method creates possible subsets
     * of length 2. 
     * It creates one of (a,b) and (b,a). Not both.
     * @param bs
     * @return 
     */
    HashSet<BitSet> createSubsetsOfLen2(BitSet bs){
        HashSet<BitSet> subsets = new HashSet<>();
        
        for(int i=0; i<bs.length();){
            i = bs.nextSetBit(i);
            
            for(int j=i+1; j<bs.length();){
                j = bs.nextSetBit(j);
                BitSet b = new BitSet(bitsetLen);
                b.set(i, true);
                b.set(j, true);
                subsets.add(b);
                j++;
            }
            i++;
        }
        return subsets;
    }
    
    
    public static void main(String[] args) {
        int min_sup = 3;
        int k = 4;
        String inputFilePath = "";
        String outputFilePath = "";

        new MineItemsets(min_sup, k, inputFilePath, outputFilePath);
    }
}
