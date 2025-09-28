import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Huffman {
    private static final int TAM = 256;
    private static  final String ARQUIVOHUFFCOMPRIMIDO = "arquivo_comprimido.huff";

    // para o método codificar
    private static  MinHeap minHeap;
    private static No raizGlobal = null;
    private static String[] dicionarioGlobal = null;
    private static String bitsCodificadosGlobal = null;
    private static long originalBits = 0L;
    private static long comprimidoBits = 0L;

    // para o decodificador
    private static String mensagemDecodificadaGlobal = null;

    public static void main(String[] args) {
        // análise de frequência e heap
        int[] frequencies = analyze("input.txt");

        if(frequencies != null){
            printFrequencies(frequencies);

            MinHeap heap = criarMinHeap(frequencies);
            heap.printHeap();

            No raiz = montarArvore(heap);
            imprimirArvore(raiz);
            String[] dicionario = new String[TAM];
            gerarTabelaDeCodigos(dicionario, raiz, "");

            imprimirTabelaDeCodigos(dicionario);

            raizGlobal = raiz;
            dicionarioGlobal = dicionario;
            codificar();

            imprimirResumoCompressao();
            criarArquivoHuff(frequencies);
            descomprimirArquivo(ARQUIVOHUFFCOMPRIMIDO);
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

    public static MinHeap criarMinHeap(int frequencies[]) {
        MinHeap heap = new MinHeap();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                heap.insert(new No((char) i, frequencies[i]));
            }
        }

        return heap;
    }

    public static void codificar() {
        if (dicionarioGlobal == null) {
            System.out.println("Dicionário não gerado");
            return;
        }

        StringBuilder sb = new StringBuilder( 1 << 12);
        long qtdChars = 0;

        try (FileInputStream fileInputStream = new FileInputStream("input.txt");
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader leitor = new BufferedReader(inputStreamReader)) {

            int caractereLido;
            while ((caractereLido = leitor.read()) != -1) {
                if(caractereLido >= 0 && caractereLido < TAM){
                    sb.append(dicionarioGlobal[caractereLido]);
                    qtdChars++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        bitsCodificadosGlobal = sb.toString();
        originalBits = qtdChars * 8L;
        comprimidoBits = bitsCodificadosGlobal.length();
    }

    public static void decodificar() {
        if (raizGlobal == null || bitsCodificadosGlobal == null) {
            mensagemDecodificadaGlobal = null;
            return;
        }

        StringBuilder saida = new StringBuilder();

        // só um símbolo na árvore
        if (raizGlobal.left == null && raizGlobal.right == null) {
            for (int i = 0; i < bitsCodificadosGlobal.length(); i++) {
                saida.append(raizGlobal.character);
            }
            mensagemDecodificadaGlobal = saida.toString();
            return;
        }

        No atual = raizGlobal;
        for (int i = 0; i < bitsCodificadosGlobal.length(); i++) {
            char b = bitsCodificadosGlobal.charAt(i);
            atual = (b == '0') ? atual.left : atual.right;

            if (atual.left == null && atual.right == null) {
                saida.append(atual.character);
                atual = raizGlobal; // volta pra raiz
            }
        }

        mensagemDecodificadaGlobal = saida.toString();
    }

    public static void imprimirResumoCompressao(){
        if (bitsCodificadosGlobal == null) {
            System.out.println("Nenhum dado codificado");
            return;
        }

        StringBuilder sb = new StringBuilder();

        long bytesOrig = (originalBits + 7) / 8;
        long bytesComp = (comprimidoBits + 7) / 8;
        double taxa = (originalBits == 0) ? 0.0 : (1.0 - (comprimidoBits / (double) originalBits)) * 100.0;

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 5: Resumo da Compressão");
        sb.append("\n--------------------------------------------------");
        sb.append(String.format("\nTamanho do arquivo original: %d bits (%d bytes)", originalBits, bytesOrig));
        sb.append(String.format("\nTamanho do arquivo comprimido: %d bits (%d btytes)", comprimidoBits, bytesComp));
        sb.append(String.format("\nTaxa de compressao: %.2f%%", taxa));
        sb.append("\n--------------------------------------------------");

        System.out.println(sb.toString());
    }

    private static byte[] transformarStringEmByte(String strBinaria) {
        if(strBinaria == null || strBinaria.isEmpty())
            return new byte[0];

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int len = strBinaria.length();

        byte resultado = 0;
        int bitPosicao = 7;

        for(int i = 0; i < len; i++){

            if(strBinaria.charAt(i) == '1'){
                byte mascara = (byte) (1 << bitPosicao);
                resultado = (byte) (resultado | mascara);
            }
            bitPosicao--;

            if(bitPosicao < 0) {
                buffer.write(resultado);
                resultado = 0;
                bitPosicao = 7;
            }
        }

        if(bitPosicao != 7) {
            buffer.write(resultado);
        }

        return buffer.toByteArray();
    }

    public static void criarArquivoHuff(int tabelaFrequencia[]) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Path path = Paths.get(ARQUIVOHUFFCOMPRIMIDO);

        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }else {
                Files.delete(path);
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }

        try(DataOutputStream dataOutputStream = new DataOutputStream(buffer)) {
            int numeroEntradas = 0;

            for(int k = 0; k < TAM; k++)
                if(tabelaFrequencia[k] > 0)
                    numeroEntradas++;

            dataOutputStream.writeInt(numeroEntradas);

            for(int k = 0; k < TAM; k++) {
                if(tabelaFrequencia[k] > 0) {

                    int frequencia = tabelaFrequencia[k];
                    dataOutputStream.writeByte(k);
                    dataOutputStream.writeInt(frequencia);
                }
            }

            dataOutputStream.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        long totalBits = bitsCodificadosGlobal.length();
        byte[] dadosComprimidos = transformarStringEmByte(bitsCodificadosGlobal);

        System.out.println("Bytes em decimal " + Arrays.toString(dadosComprimidos));

        try(DataOutputStream out = new DataOutputStream(new FileOutputStream(path.toFile()))) {
            out.write(buffer.toByteArray());
            out.writeLong(totalBits);
            out.write(dadosComprimidos);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int verificarBit(byte b, int i) {
        byte mascara = (byte)( 1 << i );
        return (b & mascara);
    }

    public static String descomprimirArquivo(String caminhoArquivo) {

        Path path = Paths.get(ARQUIVOHUFFCOMPRIMIDO);

        try {
            if (!Files.exists(path)) {
                throw new FileNotFoundException();
            }
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        try(DataInputStream inputStream = new DataInputStream(new FileInputStream(caminhoArquivo))) {

            int tamanhoDoConteudo = inputStream.readInt();

            if(tamanhoDoConteudo == 0)
                return null; // Arquivo não tem nada codificado

            int[] tabelaDeFrequencia = new int[TAM];
            for(int j = 0; j < tamanhoDoConteudo; j ++) {

                int simbolo = (char)inputStream.readUnsignedByte();
                int frequencia = inputStream.readInt();

                tabelaDeFrequencia[simbolo] = frequencia;
                System.out.println(String.format("Freq. %c, %d", (char)simbolo, frequencia));
            }

            MinHeap heap = criarMinHeap(tabelaDeFrequencia);
            raizGlobal = montarArvore(heap);

            long totalBits = inputStream.readLong();
            byte[] restante = inputStream.readAllBytes();

            StringBuilder bitsString = new StringBuilder();
            for (int i = 0; i < restante.length; i++) {
                int bitsNoByte = (i == restante.length - 1) ? (int)(totalBits % 8) : 8;
                if (bitsNoByte == 0) bitsNoByte = 8;

                for (int j = 7; j >= 8 - bitsNoByte; j--) {
                    boolean bit = ((restante[i] >> j) & 1) == 1;
                    bitsString.append(bit ? '1' : '0');
                }
            }

            System.out.println("Bits reconstruídos: " + bitsString.toString());

            bitsCodificadosGlobal = bitsString.toString();
            decodificar();

            System.out.println(mensagemDecodificadaGlobal);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }
}