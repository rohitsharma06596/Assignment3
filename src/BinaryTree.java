import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import static java.lang.Math.abs;

public class BinaryTree {
    static class Node {
        String data;
        int position;
        int hits;
        boolean isLeaf;

        Node(String data, int position, int hits) {
            this.data = data;
            this.position = position;
            this.hits = hits;
        }
    }

    static String[] data;
    static String data1;
    static ArrayList<String> value;
    static ArrayList<Node> holder;

    public static void main(String[] Args) {
        Scanner sc =  new Scanner(System.in);
        String str = sc.nextLine();
        String ch = sc.nextLine();
        int distance = sc.nextInt();
        BinaryTree block = new BinaryTree(str);
        holder = new ArrayList<Node>();
        Node temp = new Node(value.get(0), 0, 0);
        holder.add(temp);
        for (int i = 1; i < value.size(); i++) {
            boolean check = isLeaf(value.get(i));
            int pos = getLatestParent();
            boolean check1 = additem(check, i, pos);
        }
        int size = highestPos();
        String[] FinalTree = new String[size];
        for(int i = 0 ; i< FinalTree.length; i++){
            FinalTree[i] = findElement(i);
        }
        for(int i = 0 ; i< FinalTree.length; i++){
            if(FinalTree[i] == null){
                FinalTree[i] = "@#$%";
            }
            if((FinalTree[i] != "@#$%")&&(!(FinalTree[i].equals(" ")))) {
                System.out.print(FinalTree[i]+" ");
            }
        }
        Integer[] dist = new Integer[FinalTree.length];
        int tempDist = 0;
        for(int i=0; i<FinalTree.length; i++){
            int curTermIndex = i;
            int j1 = 2 * i + 1;
            int j2 = 0;
            if((2 * i + 2)<FinalTree.length){
                j2 = 2 * i + 2;
            }
            else{
                j2 = FinalTree.length-1;
            }

            tempDist = tempDist+1;
            for(int k = curTermIndex+1; k<=j2; k++){
                dist[k] = tempDist;
            }
            if(!(j1+1 > FinalTree.length)){
                i = j1;
            }
            else{
                i = j2;
            }

        }
        dist[0] = 0;
        Integer[] relDistance = new Integer[dist.length];
        int findIndex = findItem(FinalTree, ch);
        for(int i =0; i<dist.length ; i++){
            if(FinalTree[i]!= "@#$%") {
                int ancestor = findLowestAncestor(FinalTree, i, findIndex);
                relDistance[i] = abs(dist[i] + dist[findIndex] - 2 * (dist[ancestor]));
                if ((relDistance[i] == distance) && (!(FinalTree[i].equals(ch)))) {
                    System.out.print(FinalTree[i]+" ");
                }
            }
        }




    }

    BinaryTree(String str) {
        value = new ArrayList<String>(10);
        int count = 0;
        int index = 0;
        while (!(str.isEmpty())) {
            index = str.indexOf(" ");
            if ((index >= str.length()) || (index == -1)) {
                index = str.length() - 1;
            }
            value.add(str.substring(count, index));
            str = str.substring(index + 1);
            count = 0;

        }

    }

    static boolean isLeaf(String str) {
        if (str.endsWith(")") && str.startsWith("(")) {
            return true;
        } else {
            return false;
        }
    }

    static int getLatestParent() {
        int i;
        for (i = holder.size() - 1; i >= 0; i--) {
            if (holder.get(i).isLeaf == false) {
                return i;
            }
        }
        return i;
    }

    static int getParentBefore(int pos) {
        int i;
        for (i = pos-1; i >= 0; i--) {
            if (holder.get(i).isLeaf == false) {
                return i;
            }
        }
        return i;
    }

    static boolean additem(boolean check, int i, int pos) {
        int len = holder.get(pos).position;
        if (holder.get(pos).hits == 0) {
            Node temp1 = new Node(value.get(i), 2 * len + 1, 0);
            temp1.isLeaf = check;
            holder.add(temp1);
        } else if ((holder.get(pos).hits == 1)) {
            Node temp1 = new Node(value.get(i), 2 * len + 2, 0);
            temp1.isLeaf = check;
            holder.add(temp1);
        } else if (holder.get(pos).hits >= 2) {
            int posi = getParentBefore(pos);
            boolean hold = additem(check, i, posi);

        }
        holder.get(pos).hits += 1;

        return true;
    }
    static int highestPos(){
        int max = 0;
        for(int i=0; i<holder.size(); i++){
            if(max>holder.get(i).position){
                max = max;
            }
            else{
                max = holder.get(i).position;
            }
        }
        return max+1;
    }

    static String findElement(int pos){
        for( int i = 0; i<holder.size(); i++){

            if(holder.get(i).position == pos){
                String trimmed = trimString(holder.get(i).data);
                return trimmed;
            }
        }
        return null;
    }
    static String trimString(String str){
        str = str.replace('(',' ');
        str = str.replace(')',' ');
        str = str.trim();
        if(str.trim().isEmpty()){
            str = " ";
        }
        return str;
    }

    static int findItem(String[] FinalTree, String ch){
        int i;
        for(i = 0; i<FinalTree.length; i++){
            if(FinalTree[i].equals(ch)){
                break;
            }
            if(FinalTree[i].equals(null)){
                continue;
            }
        }
        return i;
    }

    static int findLowestAncestor(String[] FinalTree, int ch1, int ch2){
        ArrayList<Integer> ancFirst = new ArrayList<>();
        ArrayList<Integer> commonAnc = new ArrayList<>();
        int temp = ch1;
        while(temp >= 0){
            ancFirst.add(temp);

            if(temp %2 == 0) {
                temp = (temp - 2)/2;
            }
            else{
                temp = (temp - 1)/2;
            }
        }
        temp = ch2;
        while(temp >= 0){
            if((ancFirst.contains(temp))) {
                commonAnc.add(temp);
            }
            if(temp %2 == 0) {
                temp = (temp - 2) / 2;
            }
            else{
                temp = (temp - 1) / 2;
            }
        }
        int value = Collections.max(commonAnc);
        return value;

    }
}
