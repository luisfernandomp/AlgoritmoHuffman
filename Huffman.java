import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Huffman {
    private static final int TAM = 256;

    // para o método codificar
    private static No raizGlobal = null;
    private static String[] dicionarioGlobal = null;
    private static String bitsCodificadosGlobal = null;
    private static long originalBits = 0L;
    private static long comprimidoBits = 0L;

    // para o decodificador
    private static String mensagemDecodificadaGlobal = null;
    private static String conteudoArquivoTexto;

    /* Funções utilitárias */
    public static File recuperarArquivo(String caminhoArquivo, boolean arquivoPrecisaExistir, boolean tentarCriarArquivo)
            throws IOException {

        if(!arquivoPrecisaExistir && !tentarCriarArquivo)
            throw new IllegalArgumentException("É necessário que o arquivo exista");

        Path path = Paths.get(caminhoArquivo);

        if(arquivoPrecisaExistir) {
            if (!Files.exists(path)) {
                throw new FileNotFoundException(caminhoArquivo);
            }
        }
        else {
            Files.deleteIfExists(path);
            Files.createFile(path);
        }

        return path.toFile();
    }


    public static void main(String[] args) {
        try {
            if(args.length == 0) {
                System.out.println("Informe os parâmetros de entrada");
                return;
            }

            String nomeArquivoComprimido, nomeArquivoDescomprimido;
            String acao = args[0].trim();

            switch (acao) {
                case "-c" : {
                    nomeArquivoComprimido = args[1].trim();
                    nomeArquivoDescomprimido = args[2].trim();

                    File arquivoComprimido = recuperarArquivo(nomeArquivoComprimido, true, false);
                    File arquivoDescomprimido = recuperarArquivo(nomeArquivoDescomprimido, false, true);

                    comprimirArquivo(arquivoComprimido, arquivoDescomprimido);
                    break;
                }
                case "-d" : {
                    nomeArquivoComprimido = args[1].trim();
                    nomeArquivoDescomprimido = args[2].trim();

                    File arquivoComprimido = recuperarArquivo(nomeArquivoComprimido, true, false);
                    File arquivoDescomprimido = recuperarArquivo(nomeArquivoDescomprimido, false, true);

                    descomprimirArquivo(arquivoComprimido, arquivoDescomprimido);
                    break;
                }
                default: {
                    System.out.println("Ação informada é inválida! Informe -c para compressão e -d para descompressão.");
                    break;
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int[] analyze(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader leitor = new BufferedReader(inputStreamReader)) {

            StringBuilder sb = new StringBuilder();
            int[] frequencies = new int[TAM];
            int caractereLido;
                    
            while ((caractereLido = leitor.read()) != -1) {
                if (caractereLido >= 0 && caractereLido < TAM) {
                    frequencies[caractereLido]++;
                    sb.append((char) caractereLido);
                }
                else {
                    System.out.println("Caractere ignorado: " + (char)caractereLido);
                }
            }

            conteudoArquivoTexto = sb.toString();
            return frequencies;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void printFrequencies(int[] frequencies) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 1: Tabela de Frequencia de Caracteres");
        sb.append("\n--------------------------------------------------");

        String representacao;
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                if(i == 10) representacao = "\\n";
                else if(i == 13) representacao = "\\r";
                else representacao = String.valueOf((char)i);

                sb.append(
                        String.format("\nCaractere '%s' (ASCII: %d): %d", representacao, i, frequencies[i])
                );
            }
        }

        System.out.println(sb);
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

        String representacao;

        if(n.character == '\n') representacao = "\\n";
        else if(n.character == '\r') representacao = "\\r";
        else representacao = String.valueOf(n.character);

        String rotulo = eRaiz ? "(RAIZ, " + n.frequencie + ")"
                        : (folha ? "('" + representacao + "', " + n.frequencie + ")"
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
                String representacao;
                if(i == 10) representacao = "\\n";
                else if(i == 13) representacao = "\\r";
                else representacao = String.valueOf((char)i);

                sb.append(
                        String.format("\nCaractere '%s': %s", representacao, dicionario[i])
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

        StringBuilder sb = new StringBuilder(conteudoArquivoTexto.length());
        long qtdChars = 0;
        int caractereLido = 0;

        for(int i = 0, len = conteudoArquivoTexto.length(); i < len; i++) {
            caractereLido = conteudoArquivoTexto.charAt(i);

            if(caractereLido < TAM){
                sb.append(dicionarioGlobal[caractereLido]);
                qtdChars++;
            }
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

    public static void descomprimirArquivo(File arquivoComprimido, File arquivoDescomprimido) {
        try(DataInputStream inputStream = new DataInputStream(new FileInputStream(arquivoComprimido))) {

            int tamanhoDoConteudo = inputStream.readInt();

            if(tamanhoDoConteudo == 0)
                return; // Arquivo não tem nada codificado

            int[] tabelaDeFrequencia = new int[TAM];
            for(int j = 0; j < tamanhoDoConteudo; j ++) {

                int simbolo = (char)inputStream.readUnsignedByte();
                int frequencia = inputStream.readInt();

                tabelaDeFrequencia[simbolo] = frequencia;
            }

            MinHeap heap = criarMinHeap(tabelaDeFrequencia);
            raizGlobal = heap.montarArvore();

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

            bitsCodificadosGlobal = bitsString.toString();
            decodificar();
            Files.writeString(arquivoDescomprimido.toPath(), mensagemDecodificadaGlobal);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void comprimirArquivo(File arquivoDescomprimido, File arquivoComprimido)
    {
        try {
            int[] frequencies = analyze(arquivoDescomprimido);

            if(frequencies == null) {
                System.out.println("Não foi possível definir a frequência do texto!");
                return;
            }

            printFrequencies(frequencies);
            MinHeap heap = criarMinHeap(frequencies);
            heap.printHeap();
            No raiz = heap.montarArvore();
            imprimirArvore(raiz);
            String[] dicionario = new String[TAM];
            gerarTabelaDeCodigos(dicionario, raiz, "");
            imprimirTabelaDeCodigos(dicionario);
            raizGlobal = raiz;
            dicionarioGlobal = dicionario;
            codificar();
            imprimirResumoCompressao();

            criarArquivoComprimido(frequencies, arquivoComprimido);

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void criarArquivoComprimido(int[] frequencies, File arquivoComprimido) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try(DataOutputStream dataOutputStream = new DataOutputStream(buffer)) {
            int numeroEntradas = 0;

            for(int k = 0; k < TAM; k++)
                if(frequencies[k] > 0)
                    numeroEntradas++;

            dataOutputStream.writeInt(numeroEntradas);

            for(int k = 0; k < TAM; k++) {
                if(frequencies[k] > 0) {

                    int frequencia = frequencies[k];
                    dataOutputStream.writeByte(k);
                    dataOutputStream.writeInt(frequencia);
                }
            }

            dataOutputStream.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        long totalBits = bitsCodificadosGlobal.length();
        byte[] dadosComprimidos = transformarStringEmByte(bitsCodificadosGlobal);

        try(DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoComprimido))) {
            out.write(buffer.toByteArray());
            out.writeLong(totalBits);
            out.write(dadosComprimidos);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}