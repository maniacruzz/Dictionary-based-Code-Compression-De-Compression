/**
 *
 */
/* On my honor, I have neither given nor received unauthorized aid on this assignment */

/**
 * @author Amogh Rao
 * Course : Embedded Systems
 * University of Florida
 * UFID: 13118639
 */

import java.io.*;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;


class Mismatch{
    int count,firstindex,lastindex,maxconsecutivecount;
    String xor;
    Mismatch() {
        count = 0;
        firstindex = -1;
        lastindex = -1;
        maxconsecutivecount = 0;
        xor="";
    }

    public void printnode(){
        System.out.println("Count :"+count);
        System.out.println("First :"+firstindex);
        System.out.println("Last  :"+lastindex);
        System.out.println("Max consecutive mismatch count :"+maxconsecutivecount);
    }

}
public class SIM {
    public static final String ORIGINALFILENAME = "original.txt";
    public static final String COMPRESSEDFILENAME = "compressed.txt";
    public static final String COMPOUTFILENAME = "cout.txt";
    public static final String DECOMPOUTFILENAME = "dout.txt";
    public static final String[] int2bin2bits = {"00","01","10","11"};
    public static final String[] int2bin = {"000","001","010","011","100","101","110","111"};
    public static final String[] int2bin4bits = {"0000","0001","0010","0011","0100","0101","0110","0111","1000","1001","1010","1011","1100","1101","1110","1111"};
    public static final String[] int2bin5bits = {"00000","00001","00010","00011","00100","00101","00110","00111","01000","01001","01010","01011","01100","01101","01110","01111","10000","10001","10010","10011","10100","10101","10110","10111","11000","11001","11010","11011","11100","11101","11110","11111"};
    static Map<String,Integer> frequency = new LinkedHashMap<>();
    static ArrayList<String> dictionary = new ArrayList<>();
    private static void buildDictionary() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(ORIGINALFILENAME));
            String line = br.readLine();
            while (line != null) {
                if (frequency.containsKey(line)){
                     frequency.put(line,frequency.get(line)+1);
                }
                else{
                    frequency.put(line,1);
                }

                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //print map
        /*for (String key : frequency.keySet()){
            System.out.println(key+" : "+frequency.get(key));
        }*/
        LinkedHashMap<String,Integer> sortedmap = frequency.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(x,y)->{throw new AssertionError();},LinkedHashMap::new));
        //System.out.println("---------------------------------After sort----------------------------------");
        for (String key : sortedmap.keySet()){
            if (dictionary.size()<16){
                dictionary.add(key);
            }
            else
                break;
        }
//        System.out.println(dictionary);
    }

    private static Mismatch getHamm(String a,String b){
        Mismatch node=new Mismatch();
        int conscount=0;
        Boolean mismatchfound=false;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i)!=b.charAt(i)){
                node.xor+="1";
                if (mismatchfound){
                    conscount++;
                }
                else {
                    mismatchfound = true;
                    conscount=1;
                }
                node.count++;
                if (node.firstindex==-1)
                    node.firstindex=i;
                node.lastindex=i;

            }
            else {
                node.xor+="0";
                if (conscount>node.maxconsecutivecount)
                    node.maxconsecutivecount = conscount;
                mismatchfound = false;
                conscount = 0;
            }
        }
        if (conscount>node.maxconsecutivecount)
            node.maxconsecutivecount = conscount;
        return node;
    }
    private static void compress(){
        buildDictionary();
        int rlecount = 0;
        StringBuilder sb= new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(ORIGINALFILENAME));
            String line = br.readLine();
            String prev=null;
            whileloop:
            while (line != null) {
                //Case 1 RLE (3bits)

                if (line.equalsIgnoreCase(prev) ){
                    if (rlecount<8){
                      rlecount++;
                        prev=line;
                        line = br.readLine();
                      continue;
                    }
                    else{
                        //System.out.println(prev+"  --> 001 "+int2bin[rlecount-1]);
                        sb.append("001"+int2bin[rlecount-1]);
                        rlecount=0;
                    }
                }
                    if (rlecount>0) {
                        //System.out.println(prev + "  --> 001 " + int2bin[rlecount - 1]);
                        sb.append("001"+int2bin[rlecount-1]);
                        rlecount = 0;
                    }

                //Direct Match check 111 (4bits)
                for (int i=0;i<dictionary.size();i++){
                    if (line.equals(dictionary.get(i))){

                        //System.out.println(line + "  --> 111 "+int2bin4bits[i]);
                        sb.append("111"+int2bin4bits[i]);
                        //System.out.println(sb);
                        prev=line;
                        line = br.readLine();
                        continue whileloop;
                    }
                }

                //1-bit (011), 2-bit (100), 4-bit (101) consecutive mismatch (9bits)
                for (int i=0;i<dictionary.size();i++){
                    Mismatch node = getHamm(line,dictionary.get(i));
                    if (node.count==3 || node.count>4)
                        continue;
                    else if(node.count==node.maxconsecutivecount) {
                        //do 1 of the 3
                        if (node.maxconsecutivecount==1){
                            //System.out.println(line+"  --> 011 "+int2bin5bits[node.firstindex]+" "+int2bin4bits[i]);
                            sb.append("011"+int2bin5bits[node.firstindex]+int2bin4bits[i]);
                        }
                        else if (node.maxconsecutivecount==2){
                            //System.out.println(line+"  --> 100 "+int2bin5bits[node.firstindex]+" "+int2bin4bits[i]);
                            sb.append("100"+int2bin5bits[node.firstindex]+int2bin4bits[i]);
                        }
                        else if (node.maxconsecutivecount==4) {
                            //System.out.println(line + "  --> 101 " + int2bin5bits[node.firstindex] + " " + int2bin4bits[i]);
                            sb.append("101"+int2bin5bits[node.firstindex]+int2bin4bits[i]);
                        }
                        //else
                            //System.out.println("Some error");
                        prev=line;
                        line = br.readLine();
                        continue whileloop;
                    }
                }

                //4bit bitmask-based compression (010) (13bits)
                for (int i=0;i<dictionary.size();i++){
                    Mismatch node = getHamm(line,dictionary.get(i));
                    if (node.count<4 && (node.lastindex-node.firstindex+1)<=4 ){

                        if (node.firstindex>28)
                            node.firstindex=28;
                        //System.out.println(line+"  --> 010 "+int2bin5bits[node.firstindex]+" "+node.xor.substring(node.firstindex,node.firstindex+4)+" "+int2bin4bits[i]);
                        sb.append("010"+int2bin5bits[node.firstindex]+node.xor.substring(node.firstindex,node.firstindex+4)+int2bin4bits[i]);
                        prev=line;
                        line = br.readLine();
                        continue whileloop;
                    }
                }

                //2-bit mismatches anywhere (110) (14bits)
                for (int i=0;i<dictionary.size();i++){
                    Mismatch node = getHamm(line,dictionary.get(i));
                    if (node.count==2){
                        //System.out.println(line+"  --> 110 "+int2bin5bits[node.firstindex]+" "+int2bin5bits[node.lastindex]+" "+int2bin4bits[i]);
                        sb.append("110"+int2bin5bits[node.firstindex]+int2bin5bits[node.lastindex]+int2bin4bits[i]);
                        prev=line;
                        line = br.readLine();
                        continue whileloop;
                    }
                }

                //System.out.println(line+"  --> 000 "+line);
                sb.append("000"+line);

                prev=line;
                line = br.readLine();
            }
            write2file(sb.toString());
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void write2file(String sb) {
        //Print to file
        try {
            System.setOut(new PrintStream(new File(COMPOUTFILENAME)));
        } catch (Exception e) {
        }
        String temp32;
        for (int i = 0; i <sb.length() ; i+=32) {
            if (i+32<sb.length())
                temp32=sb.substring(i,i+32);
            else
                temp32= String.format("%-32s",sb.substring(i)).replace(' ','0');
            System.out.println(temp32);
        }
    }

    private static void decompress() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(COMPRESSEDFILENAME));
            StringBuilder sb=new StringBuilder();
            String line = br.readLine();
//            int count=0;
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            br.close();
            String content=sb.toString();
            //System.out.println(content);
            String[] parts = content.split("xxxx");
            dictionary = new ArrayList<>(Arrays.asList(parts[1].split("\n")));
            dictionary.remove(0);
            //System.out.println(dictionary.size());
            String text=parts[0].replaceAll("[\n\r]","");
            //System.out.println(text);
            //System.out.println(text.length());
            process(text);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//            String content = new Scanner(new File(COMPRESSEDFILENAME)).useDelimiter("\\Z").next();



    }

    private static void process(String text) {
        //Print to file
        try {
            System.setOut(new PrintStream(new File(DECOMPOUTFILENAME)));
        } catch (Exception e) {
        }
        int i=0;
        String compcode;
        String prev="";
        while (i<text.length()){
            //Read the code for compression method
            if (i+3>text.length())
                break;
            compcode = text.substring(i,i+3);
            i+=3; // increment i
            switch (compcode){
                case "000":
                    if (i+32>text.length()){
                        break;
                    }
                    prev=text.substring(i,i+32);
                    System.out.println(prev);
                    i+=32;
                    break;
                case "001":
                    int count=Integer.parseInt(text.substring(i,i+3),2);
                    for (int j = 0; j <= count; j++) {
                        System.out.println(prev);
                    }
                    i+=3;
                    break;
                case "010":
                    String temp=text.substring(i,i+13);
                    i+=13;
                    String dicval=dictionary.get(Integer.parseInt(temp.substring(9),2));
                    int pos=Integer.parseInt(temp.substring(0,5),2);
                    int mask=Integer.parseInt(temp.substring(5,9),2);
                    int xor=Integer.parseInt(dicval.substring(pos,pos+4),2)^mask;
                    prev=dicval.substring(0,pos)+int2bin4bits[xor]+dicval.substring(pos+4);
                    System.out.println(prev);
                    break;
                case "011":
                    temp=text.substring(i,i+9);
                    i+=9;
                    dicval=dictionary.get(Integer.parseInt(temp.substring(5),2));
                    pos=Integer.parseInt(temp.substring(0,5),2);
                    xor=Integer.parseInt(dicval.substring(pos,pos+1))^1;
                    prev=dicval.substring(0,pos)+xor+dicval.substring(pos+1);
                    System.out.println(prev);
                    break;
                case "100":
                    temp=text.substring(i,i+9);
                    i+=9;
                    dicval=dictionary.get(Integer.parseInt(temp.substring(5),2));
                    pos=Integer.parseInt(temp.substring(0,5),2);
                    xor=Integer.parseInt(dicval.substring(pos,pos+2),2)^3;
                    prev=dicval.substring(0,pos)+int2bin2bits[xor]+dicval.substring(pos+2);
                    System.out.println(prev);
                    break;
                case "101":
                    temp=text.substring(i,i+9);
                    i+=9;
                    dicval=dictionary.get(Integer.parseInt(temp.substring(5),2));
                    pos=Integer.parseInt(temp.substring(0,5),2);
                    xor=Integer.parseInt(dicval.substring(pos,pos+4),2)^15;
                    prev=dicval.substring(0,pos)+int2bin4bits[xor]+dicval.substring(pos+4);
                    System.out.println(prev);
                    break;
                case "110":
                    temp=text.substring(i,i+14);
                    i+=14;
                    dicval=dictionary.get(Integer.parseInt(temp.substring(10),2));
                    pos=Integer.parseInt(temp.substring(0,5),2);
                    int pos2=Integer.parseInt(temp.substring(5,10),2);
                    xor=Integer.parseInt(dicval.substring(pos,pos+1))^1;
                    int xor2=Integer.parseInt(dicval.substring(pos2,pos2+1))^1;
                    prev=dicval.substring(0,pos)+xor+dicval.substring(pos+1,pos2)+xor2+dicval.substring(pos2+1);
                    System.out.println(prev);
                    break;
                case "111":
                    temp=text.substring(i,i+4);
                    i+=4;
                    prev=dictionary.get(Integer.parseInt(temp,2));
                    System.out.println(prev);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length<1){
            System.out.println("Format ./SIM [1|2]\n1 : Compression\n2 : Decompression ");
        }
        else{
            //System.out.println("In else");
            if (args[0].equals("1")){
                //System.out.println("In Compress");
                compress();
                //Mismatch node = getHamm("11111110000000100000000000001001","11111110000000100000000000001000");
                //node.printnode();
                System.out.println("xxxx");
                for (int i=0;i<dictionary.size();i++)
                    System.out.println(dictionary.get(i));
            }
            else if (args[0].equals("2")){
                //System.out.println("In Decompress");
                decompress();
            }
        }
    }

}
