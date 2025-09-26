import java.io.FileInputStream;
import java.io.IOException;

public class Huffman {
    public static void main(String[] args) {
        // análise de frequência e heap
        int[] frequencies = analyze("input.txt");
        printFrequencies(frequencies);

        MinHeap heap = new MinHeap();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                heap.insert(new No((char) i, frequencies[i]));
            }
        }
        heap.printHeap();
    }

    public static int[] analyze(String filePath) {
        int[] frequencies = new int[256];
        try (FileInputStream input = new FileInputStream(filePath)) {
            int i;
            while ((i = input.read()) != -1) {
                frequencies[i]++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frequencies;
    }

    public static void printFrequencies(int[] frequencies) {
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                System.out.println("Character '" + (char) i + "' (ASCII: " + i + "): " + frequencies[i]);
            }
        }
    }
}