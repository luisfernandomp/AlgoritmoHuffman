import java.util.ArrayList;
import java.util.PriorityQueue;

public class MinHeap {
    private ArrayList<No> heap;

    public MinHeap() {
        heap = new ArrayList<>();
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }
    
    public void insert (No node) {
        heap.add(node);
        heapifyUp(heap.size() - 1);
    }

    public No extractMin() {
        if (heap.isEmpty()) return null;
        No min = heap.get(0);
        No last = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        return min;
    }

    public No peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(index).compareTo(heap.get(parent)) < 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    private void heapifyDown(int index) {
        int size = heap.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && heap.get(left).compareTo(heap.get(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && heap.get(right).compareTo(heap.get(smallest)) < 0) {
                smallest = right;
            }
            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    public void printHeap() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 2: Min-Heap Inicial (Vetor)");
        sb.append("\n--------------------------------------------------");
        sb.append("\n[");

        ArrayList<No> heapOrdenado = ordenarListaComFilaDePrioridade();

        String representacao;
        for(int i = 0; i < heapOrdenado.size(); i++)
        {
            if(heapOrdenado.get(i).character == '\n') representacao = "\\n";
            else if(heapOrdenado.get(i).character == '\r') representacao = "\\r";
            else representacao = String.valueOf(heapOrdenado.get(i).character);

            String format = String.format(" No('%s', %d)", representacao, heapOrdenado.get(i).frequencie);
            sb.append(format);
            if(i < heapOrdenado.size() - 1) sb.append(",");
        }

        sb.append("\n]");
        System.out.println(sb.toString());
    }

    private void swap(int i, int j) {
        No temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public ArrayList<No> ordenarListaComFilaDePrioridade() {
        PriorityQueue<No> filaPrioridade = new PriorityQueue<No>(heap);
        ArrayList<No> arrayOrdenado = new ArrayList<No>(filaPrioridade.size());
        while (!filaPrioridade.isEmpty()) {
            arrayOrdenado.add(filaPrioridade.poll());
        }
        return arrayOrdenado;
    }

    public No montarArvore() {
        No primeiroMenor, segundoMenor, novo;

        while(this.size() > 1) {
            primeiroMenor = this.extractMin();
            segundoMenor = this.extractMin();

            int frequenciaNovoNo = primeiroMenor.frequencie + segundoMenor.frequencie;
            novo = new No('\0', frequenciaNovoNo);
            novo.left = primeiroMenor;
            novo.right = segundoMenor;

            this.insert(novo);
        }

        return this.peek();
    }
}
