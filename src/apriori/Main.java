/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package apriori;

/**
 *
 * @author Sudipta Kar <cryptexcode@gmail.com>
 */
public class Main {
    
    public static void main(String[] args) {
        try{
            // min_sup k input_transaction_file_path output_file_path
            int min_sup             = Integer.parseInt(args[0]);
            int k                   = Integer.parseInt(args[1]);
            String inputFilePath    = args[2];
            String outputFilePath   = args[3];
            
            new MineItemsets(min_sup, k, inputFilePath, outputFilePath);
        }
        catch(Exception e){
            e.printStackTrace();
            System.err.println("Please check the input format");
            System.err.println("<min_sup> <k> <input_transaction_file_path> <output_file_path>");
        }
    }
    
}
