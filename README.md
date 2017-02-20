# AprioriAssociationMining
A fast implementation of Apriori Association Mining algorithm. It uses Java Bitset data structure to make operations faster and uses several pruning techniques to improve the performance. It usually take a few seconds to mine the associations.

How to Run
----------
It takes 4 parameters. 
a) minimum support
b) k
c) transaction file path
d) output file path


**1. Run from jar:**
cd to the source directory.  Command structure is following.
 * `java -jar dist/Apiori_DM.jar <minimum_support> <k> <input_file_path> <output_file_path>`
 * Example command:

 * `  java -jar dist/Apiori_DM.jar 4 3 /WorkSpace/Courseworks/COSC_6335_DM/Project1/proj_1/transactionDB.txt /WorkSpace/Courseworks/COSC_6335_DM/Project1/my_outputs/sup_4_k_3.txt`

**2. Run from source code:**
* The code is in MineItemsets class.
* The constructor takes the 4 input parameters same as jar.
* Call this constructor from somewhere like following.

* `public static void main(String[] args) {
        int min_sup = 3;
        int k = 4;
        String inputFilePath = "";
        String outputFilePath = "";
         new MineItemsets(min_sup, k, inputFilePath, outputFilePath);
}`