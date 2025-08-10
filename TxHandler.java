/*  Muhammad Absar Khalid
 * Blockchain & CryptoCurrencies
 */



import java.util.HashSet;


import java.util.Set;


import java.util.ArrayList;


public class TxHandler 
{
    
    private UTXOPool coin_data;


    public TxHandler(UTXOPool utxoPool) 

    {

        this.coin_data = new UTXOPool(utxoPool);

    }

    public boolean isValidTx(Transaction tx) 

    {


/** Returns true if
***  (1)
    all outputs claimed by tx are in the current UTXO pool


*** (2) 
    the signatures on each input of tx are valid,



*** (3)
    no UTXO is claimed multiple times by tx.......

**/

        Set<UTXO> utxoSet = new HashSet<UTXO>();

        double add_data_in = 0;

        int counter=0;

        
        while(counter < tx.numInputs())

        {


            Transaction.Input input = tx.getInput(counter);

            UTXO temp = new UTXO(input.prevTxHash, input.outputIndex);

            if (utxoSet.contains(temp))


                return false;



            utxoSet.add(temp);

            if (coin_data.contains(temp)) 
            {


                Transaction.Output output = coin_data.getTxOutput(temp);


                if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(counter), input.signature)) 
                {


                    return false;


                }

                add_data_in += output.value;
            } 
            
            else 
            {


                return false;


            }

            counter++;


        }



        double add_data_out = 0;


        counter=0;


        while (counter < tx.numOutputs())


        {
            Transaction.Output output = tx.getOutput(counter);




            if (output.value >= 0) 
            
            {

                add_data_out += output.value;

            } 
            else 
            {


                return false;

            }
            counter++;
        }


        
        return add_data_in >= add_data_out;
    }






    /**
     * Handles each epoch :
     * By receiving an unordered array of proposed transactions, checking each
      
     
     * Transaction for correctness:
      Returning a mutually valid array of accepted transactions.


     * update:
      the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) 


    {


        ArrayList<Transaction> tranx = new ArrayList<Transaction>();

        int count=0;

        while (count < possibleTxs.length) 

        {
            tranx.add(possibleTxs[count]);
            count++;
        }

        boolean tx_valid = false;

        ArrayList<Transaction> final_updated_List = new ArrayList<Transaction>();

        do 
        {
            tx_valid = false;

            Transaction true_Trx = null;

            for (Transaction tx: tranx) 

            {
                if (isValidTx(tx)) 
                
                {

                    true_Trx = tx;

                    tx_valid = true;

                    // Updateing UTXO pool


                    for (Transaction.Input input: tx.getInputs()) 

                    {

                        coin_data.removeUTXO(new UTXO(input.prevTxHash, input.outputIndex));
                    }
                    int counter1=0;
                    while(counter1 < tx.numOutputs())
                    {

                        coin_data.addUTXO(new UTXO(tx.getHash(), counter1), tx.getOutput(counter1));
                        counter1++;
                    }


                    break;

                }
            }

            if (true_Trx != null) 
            
            {
                final_updated_List.add(true_Trx);

                tranx.remove(true_Trx);
            }


        } while (tx_valid && tranx.size() > 0);

        Transaction[] finalize = new Transaction[final_updated_List.size()];

        finalize = final_updated_List.toArray(finalize);

        return finalize;
    }





}
