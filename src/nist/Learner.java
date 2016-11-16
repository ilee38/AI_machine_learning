package nist;

import java.io.*;
import java.util.*;
import java.lang.Math;

public class Learner implements Runnable {
    Scanner in; // for reading networks
    PrintStream out; // for printing out solutions
    String line;
    int[] input;
    int n = 0;	//counter for number of examples
	int[] nd = new int[10];		//counter for # of examples where label = d
	int[][] ndi = new int[10][65];		//counter for # of ex. where label=d and xi = 1. Col. 0 is left blank
	double[][] digitWeights = new double[10][65];
	double [] Yd = new double[10];	//array of Yd for each d
	
	private static final double LEARNING_RATE =0.5;
	private static final int POSSIBLE_LABELS = 10;	//Number of possible labels (digits 0 thru 9)

    public Learner(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public void run() {
        input = new int[65];
        line = in.nextLine();		//get first example image
        initCounters();		//counters used by the Naive Bayes algorithm
        //initWeights();		//weights used by the linear learning algorithm
        while (! line.equals("quit")) {
            input[0] = 1; // for bias weight
            for (int i = 0; i < 64; i++) {
                char c = line.charAt(i);
                input[i + 1] = (c == '1') ? 1 : 0;
            }

	    // replace this with a naive Bayes prediction
            
          int prediction = NaiveBayesPredictor(input, n, nd, ndi);
          //int prediction = linearClassifier(input);

            out.print(prediction);
            out.print('\n');

            line = in.nextLine();

            int label = 0;
            if (line.startsWith("incorrect")) {
                label = line.charAt(20) - 48;
            } else {
                label = line.charAt(18) - 48;
            }
	    
	    // use label to update naive Bayes counters  
            
            updateCounters(label);
            //updateWeights(prediction, label, input);

            line = in.nextLine();		//gets next example image
        }

        in.close();
        out.close();
    }
    
   
/*
 * Returns a number prediction for the corresponding image (bit string). 
 * Uses a Naive Bayes algorithm implementation to perform the predictions.
 * 
 * */    
    public int NaiveBayesPredictor(int[] input, int n, int[] nd, int[][] ndi){
    	double[] PofY = new double[10];
    	double[][] PofXis1 = new double[65][10];
    	double[][] PofXis0 = new double[65][10];
    	double[] PofYgivenX = new double[10];
    	double pFactor = 0.0;
    	for(int d = 0; d < POSSIBLE_LABELS; d++){
    		PofY[d] = (nd[d] + 1.0) / (n + 10.0);	//Calculate P(Y=d) for each d
    		for(int i = 1; i < input.length; i++){	//Calculate P(xi=1|Y=d) and P(xi=0|Y=d) for each i for every d
    			PofXis1[i][d] = (ndi[d][i] + 1.0) / (nd[d] + 2.0);
    			PofXis0[i][d] = ((nd[d] - ndi[d][i]) + 1.0) / (nd[d] + 2.0);
    			if(input[i] == 1){		//For the current image (input), get P(Y=d|x1, x2,...,x64) for each d
    				pFactor += Math.log10(PofXis1[i][d]);
    			}else{
    				pFactor += Math.log10(PofXis0[i][d]);
    			}
    		}
    		PofYgivenX[d] = Math.log10(PofY[d]) + pFactor;
    		pFactor = 0.0;		//reset pFactor
    	}
    	return getHighestProb(PofYgivenX);
    }
    
    
/*
 * Finds the highest probability value from the PofYgivenX array.
 * Returns the array index of this value, which represents the value
 * of the prediction (or label) 
 * */
    public int getHighestProb(double[] PofYgivenX){
    	double maxProb = PofYgivenX[0];	
    	int index = 0;
    	for(int i = 1; i < PofYgivenX.length; i++){
    		if(PofYgivenX[i] > maxProb){
    			maxProb = PofYgivenX[i];
    			index = i;
    		}
    	}
    	return index;
    }
 
    
 /*
  * Initializes counters nd and ndi to 0
  * 
  * */
    public void initCounters(){
    	for(int i = 0; i < POSSIBLE_LABELS; i++){
    		nd[i] = 0;
    		for(int j = 1; j < 65; j++){
    			ndi[i][j] = 0;
    		}
    	}
    }
    
    
/*
 * Updates the counters after each example is processed
 * 
 * */    
    public void updateCounters(int label){
    	n +=1;
    	nd[label] += 1;
    	for(int i = 1; i < input.length; i++){
    		if(input[i] == 1){
    			ndi[label][i] += 1;
    		}
    	}
    }
    

    
 
/*  **************************************************************************** 
 *  CODE FOR LINEAR LEARNING (NOT USED). STILL NEEDS TO BE WORKED ON TO PRODUCE
 *  A BETTER ERROR RATE 
 *  
 *  *****************************************************************************/    
    /*
     * Returns a prediction on the label, using Linear learning
     * 
     * */
        public int linearClassifier(int[] input){
        	int prediction = 0;
        	double highestYd = 0.0;
        	highestYd = Yd[0];
        	for(int i = 1; i < Yd.length; i++){
        		if(Yd[i] > highestYd){
        			highestYd = Yd[i];
        			prediction = i;
        		}
        	}
        	return prediction;
        }
        

    /*
     * Updates the weights based on the label and prediction values
     * it uses the logistic loss function for the update
     * */
        public void updateWeights(int prediction, int label, int[] input){
        	double delta = 0.0;
        	delta = label / (1 + Math.exp(label * Yd[label]));    //label - (1 / (1 + Math.exp(-Yd[d]))); 
    		for(int i = 0; i < input.length; i++){
    			digitWeights[label][i] = digitWeights[label][i] + (LEARNING_RATE * delta * input[i]);
    		}
    	    for(int j = 0; j < input.length; j++){
    	    	Yd[label] += digitWeights[label][j] * input[j];
    	    }
        }
        
        
    /*
     * Initializes all weights to 0
     * 
     * */       
        public void initWeights(){
        	for(int d = 0; d < POSSIBLE_LABELS; d++){
        		Yd[d] = 0.0;
        		for(int j = 0; j < input.length; j++){
        			digitWeights[d][j] = 0.0;
        		}
        	}
        }
        
 /*************************** END OF LINEAR LEARNING CODE **********************/  
    
    
    public static final String[] cleanImages = {
            "0000000000111100010000100100001001000010010000100100001000111100",
            "0000000000001000000010000000100000001000000010000000100000001000",
            "0000000000111100010000100000010000001000000100000010000001111110",
            "0000000000111100010000100000001000111100000000100100001000111100",
            "0000000001000100010001000100010001111110000001000000010000000100",
            "0000000001111110010000000100000001111100000000100000001001111100",
            "0000000000000000000000000000000000000000000000000000000000000000",
            "0000000000111100010000100100000001111100010000100100001000111100",
            "0000000001111110000000100000010000001000000100000010000001000000",
            "0000000000111100010000100100001000111100010000100100001000111100",
            "0000000000111100010000100100001000111110000000100100001000111100",
            "0011110001000010010000100100001001000010010000100011110000000000",
            "0001000000010000000100000001000000010000000100000001000000000000",
            "0011110001000010000001000000100000010000001000000111111000000000",
            "0011110001000010000000100011110000000010010000100011110000000000",
            "0100010001000100010001000111111000000100000001000000010000000000",
            "0111111001000000010000000111110000000010000000100111110000000000",
            "0011110001000010010000000111110001000010010000100011110000000000",
            "0111111000000010000001000000100000010000001000000100000000000000",
            "0011110001000010010000100011110001000010010000100011110000000000",
            "0011110001000010010000100011111000000010010000100011110000000000" };

    public static void main(String[] args) throws Exception {
        PipedOutputStream pipeout = new PipedOutputStream();
        PipedInputStream pipein;
        try {
            pipein = new PipedInputStream(pipeout);
        } catch (Exception e) {
            throw new RuntimeException("pipe failed " + e);
        }
        Scanner agentIn = new Scanner(pipein);
        PrintStream printToAgent = new PrintStream(pipeout, true);
        pipeout = new PipedOutputStream();
        try {
            pipein = new PipedInputStream(pipeout);
        } catch (Exception e) {
            throw new RuntimeException("pipe failed " + e);
        }
        Scanner readFromAgent = new Scanner(new InputStreamReader(pipein));
        PrintStream agentOut = new PrintStream(pipeout, true);

        Runnable agent = new Learner(agentIn, agentOut);
        Thread athread = new Thread(agent);
        athread.start();

        int correct = 0, incorrect = 0;
        for (int i = 0; i < 1000; i++) {
            String image = cleanImages[i % 20];
            int label = i % 10;
            printToAgent.print(image);
            printToAgent.print('\n');
            // System.out.println(image);
            while (!readFromAgent.hasNext()) {
                Thread.sleep(0);
            }
            int prediction = readFromAgent.nextInt();
            String line = "";
            if (prediction == label) {
                line += "correct ";
                correct++;
            } else {
                line += "incorrect ";
                incorrect++;
            }
            line += String.format("(label is %d, error rate = %d/%d = %.2f)",
                    label, incorrect, correct + incorrect, 100
                            * (0.0 + incorrect) / (0.0 + correct + incorrect));
            printToAgent.print(line);
            printToAgent.print('\n');
            int linetest = 10;
            while (linetest < i + 1)
                linetest *= 10;
            linetest /= 10;
            if ((i + 1) % linetest == 0) {
                System.out.println(image);
                System.out.println(prediction);
                System.out.println(line);
            }
        }
        printToAgent.print("quit");
        printToAgent.print('\n');
        athread.join();
        agentIn.close();
        agentOut.close();
        readFromAgent.close();
        printToAgent.close();

    }
}
