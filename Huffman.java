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
            imprimirArvore(raiz);
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

    private static String rotulo(No n, boolean eRaiz) {
        boolean folha = (n.left == null && n.right == null);
        if (eRaiz) return "(RAIZ, " + n.frequencie + ")";
        else if (folha) return "('" + n.character + "', " + n.frequencie + ")";
        else return "(N, " + n.frequencie + ")";
    }

    public static void imprimirArvore(No raiz) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("ETAPA 3: Impressão da Árvore de Huffman");
        System.out.println("--------------------------------------------------");
        
        imprimirArvoreRec(raiz, "", true, true);
    }

    private static void imprimirArvoreRec(No n, String prefix, boolean eUltimo, boolean eRaiz) {
        if (n == null) return;

        boolean folha = (n.left == null && n.right == null);
        String rotulo = eRaiz ? "(RAIZ, " + n.frequencie + ")"
                        : (folha ? "('" + n.character + "', " + n.frequencie + ")"
                                : "(N, " + n.frequencie + ")");

        if (eRaiz) {
            System.out.println(rotulo);
        } else {
            System.out.println(prefix + (eUltimo ? "└── " : "├── ") + rotulo);
        }

        java.util.List<No> filhos = new java.util.ArrayList<>();
        if (n.left  != null) filhos.add(n.left);
        if (n.right != null) filhos.add(n.right);

        for (int i = 0; i < filhos.size(); i++) {
            boolean ultimo = (i == filhos.size() - 1);
            String nextPrefix = prefix + (eRaiz ? "" : (eUltimo ? "    " : "│   "));
            imprimirArvoreRec(filhos.get(i), nextPrefix, ultimo, false);
        }
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

    public static void imprimirResumoCompressao(){
        //TODO : Implementar método para exibir resumo compressão
    }
}