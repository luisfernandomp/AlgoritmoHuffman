import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Huffman {
    private static final int TAM = 256;

    public static void main(String[] args) {
        // análise de frequência e heap
        int[] frequencies = analyze("input.txt");

        if(frequencies != null){
            printFrequencies(frequencies);

            MinHeap heap = new MinHeap();
            for (int i = 0; i < frequencies.length; i++) {
                if (frequencies[i] > 0) {
                    heap.insert(new No((char) i, frequencies[i]));
                }
            }
            heap.printHeap();

            No raiz = montarArvore(heap);
            String[] dicionario = new String[TAM];
            gerarTabelaDeCodigos(dicionario, raiz, "");

            imprimirTabelaDeCodigos(dicionario);
        }
    }

    public static int[] analyze(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader leitor = new BufferedReader(inputStreamReader)) {

            int[] frequencies = new int[TAM];

            int caractereLido;
            while ((caractereLido = leitor.read()) != -1) {
                if(caractereLido >= 0 && caractereLido < TAM){
                    frequencies[caractereLido]++;
                }
            }

            return frequencies;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void printFrequencies(int[] frequencies) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 1: Tabela de Frequencia de Caracteres");
        sb.append("\n--------------------------------------------------");

        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                sb.append(
                        String.format("\nCaractere '%c' (ASCII: %d): %d", (char)i, i, frequencies[i])
                );
            }
        }

        System.out.println(sb.toString());
    }

    public static No montarArvore(MinHeap minHeap) {
        No primeiro, segundo, novo;

        while(minHeap.size() > 1) {
            primeiro = minHeap.extractMin();
            segundo = minHeap.extractMin();

            int frequenciaNovoNo = primeiro.frequencie + segundo.frequencie;
            novo = new No('\0', frequenciaNovoNo);
            novo.left = primeiro;
            novo.right = segundo;

            minHeap.insert(novo);
        }

        return minHeap.peek();
    }

    public static void gerarTabelaDeCodigos(String[] dicionario, No raiz, String caminho) {
        if(raiz.left  == null && raiz.right == null) {
            dicionario[(int)raiz.character] = caminho;
        }
        else {
            gerarTabelaDeCodigos(dicionario, raiz.left, caminho + "0");
            gerarTabelaDeCodigos(dicionario, raiz.right, caminho + "1");
        }
    }

    public static void imprimirTabelaDeCodigos(String[] dicionario) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 4: Tabela de Codigos de Huffman");
        sb.append("\n--------------------------------------------------");

        for (int i = 0; i < dicionario.length; i++) {
            if(dicionario[i] != null) {
                sb.append(
                        String.format("\nCaractere '%c': %s", (char)i, dicionario[i])
                );
            }
        }

        System.out.print(sb.toString());
    }

    public static void codificar() {
        //TODO: Implementar método para codificar
    }

    public static void decodificar() {
        //TODO: Implementar método para decodificar
    }

    public static void imprimirArvore(No raiz) {
        //TODO : Implementar método para exibir árvore
    }

    public static void imprimirResumoCompressao(){
        //TODO : Implementar método para exibir resumo compressão
    }
}