import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
public class Viterbi {
    HashMap<String, HashMap<String, Double>> transitions = new HashMap<>();
    HashMap<String, HashMap<String, Double>> observations = new HashMap<>();

    public void count(String sentenceF, String tagF) throws IOException {
        BufferedReader sentenceFile = new BufferedReader(new FileReader(sentenceF));
        BufferedReader tagFile = new BufferedReader(new FileReader(tagF));
        String sentenceLine = sentenceFile.readLine();
        String tagLine = tagFile.readLine();

        while(sentenceLine != null) {

            String[] words = sentenceLine.split(" ");
            String[] tags = tagLine.split(" ");
            String prev = "#";

            for(int i = 0; i < words.length; i++) {
                if (!observations.containsKey(tags[i])) {
                    observations.put(tags[i], new HashMap<>());
                }
                if (!observations.get(tags[i]).containsKey(words[i])) {
                    observations.get(tags[i]).put(words[i], 1.0);
                } else {
                    observations.get(tags[i]).put(words[i], 1.0 + observations.get(tags[i]).get(words[i]));
                }

                if (!transitions.containsKey(prev)) {
                    transitions.put(prev, new HashMap<>());
                }
                if (!transitions.get(prev).containsKey(tags[i])) {
                    transitions.get(prev).put(tags[i], 1.0);
                } else {
                    transitions.get(prev).put(tags[i], 1.0 + transitions.get(prev).get(tags[i]));
                }
                prev = tags[i];
            }
            sentenceLine = sentenceFile.readLine();
            tagLine = tagFile.readLine();
        }

        for(String k : transitions.keySet()){
            int total = 0;
            for(String m : transitions.get(k).keySet()){
                total += transitions.get(k).get(m);
            }
            for(String m : transitions.get(k).keySet()){
                transitions.get(k).replace(m, Math.log(transitions.get(k).get(m) / total));
            }
        }

        for(String k : observations.keySet()){
            int total = 0;
            for(String m : observations.get(k).keySet()){
                total += observations.get(k).get(m);
            }
            for(String m : observations.get(k).keySet()){
                observations.get(k).replace(m, Math.log(observations.get(k).get(m) / total));
            }
        }

    }

    public ArrayList<String> viterbi(String sentence){
        ArrayList<Map<String, String>> backtrace = new ArrayList<>();
        String start = "#";
        HashSet<String> currStates = new HashSet<>();
        currStates.add(start);
        HashMap<String, Double> currScores = new HashMap<>();
        currScores.put(start, 0.0);
        String[] words = sentence.split(" ");
        for(int i = 0; i < words.length; i++){
            HashSet<String> nextStates = new HashSet<>();
            HashMap<String, Double> nextScores = new HashMap<>();
            backtrace.add(new HashMap<>());
            for(String s : currStates){
                if(transitions.containsKey(s)){
                    for(String n : transitions.get(s).keySet()){
                        nextStates.add(n);
                        double nextScore;
                        if(observations.get(n).containsKey(words[i])){
                            nextScore = currScores.get(s) + transitions.get(s).get(n) + observations.get(n).get(words[i]);
                        }else{
                            nextScore = currScores.get(s) + transitions.get(s).get(n) - 100;
                        }
                        if(!nextScores.containsKey(n) || nextScore > nextScores.get(n)){
                            nextScores.put(n, nextScore);
                            backtrace.get(i).put(n, s);
                        }
                    }
                }
            }
            currStates = nextStates;
            currScores = nextScores;
        }

        double max = Integer.MIN_VALUE;
        String tag = "";
        for (String s : currScores.keySet()) {
            if (currScores.get(s) > max) {
                max = currScores.get(s);
                tag = s;
            }
        }

        ArrayList<String> output = new ArrayList<>();
        for (int i = words.length - 1; i >=  0; i--) {
            output.add(0, tag);
            tag = backtrace.get(i).get(tag);
            //System.out.println(i);
        }

        return output;
    }

    public String viterbiFiles(String sentenceFile, String tagFile) throws IOException{
        BufferedReader sentenceReader = new BufferedReader(new FileReader(sentenceFile));
        BufferedReader tagReader = new BufferedReader(new FileReader(tagFile));
        ArrayList<ArrayList<String>> allTags = new ArrayList<>();
        ArrayList<ArrayList<String>> givenTags = new ArrayList<>();
        int count = 0;
        int total = 0;

        while(sentenceReader.ready()){
            String line = sentenceReader.readLine();
            allTags.add(new ArrayList<>(viterbi(line)));
        }
        while(tagReader.ready()){
            String tagLine = tagReader.readLine();
            String[] tagLines = tagLine.split(" ");
            ArrayList<String> tagLines1 = new ArrayList<>();
            for(int i = 0; i < tagLines.length; i++){
                tagLines1.add(tagLines[i]);
            }
            givenTags.add(tagLines1);
        }
        for(int i = 0; i < allTags.size(); i++){
            for(int j = 0; j < allTags.get(i).size(); j++){
                if(allTags.get(i).get(j).equals(givenTags.get(i).get(j))){
                    count++;
                    total++;
                }
                else{
                    total++;
                }
            }
        }
        String output = "Viterbi got " + count + " tags right and " + (total-count) + " wrong.";
        return output;
    }

    public static void main(String[] args) throws IOException {
        Viterbi v = new Viterbi();
        v.count("PS5/brown-train-sentences.txt", "PS5/brown-train-tags.txt");
//        System.out.println(v.observations);
//        System.out.println(v.transitions);
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Type a sentence");
        String input = keyboard.nextLine();
        System.out.println(v.viterbi(input));
        System.out.println(v.viterbiFiles("PS5/brown-test-sentences.txt", "PS5/brown-test-tags.txt"));
        System.out.println(v.viterbiFiles("PS5/my-text.txt", "PS5/my-text-tags.txt"));
    }
}
