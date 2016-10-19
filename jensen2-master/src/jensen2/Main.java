package jensen2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static int numeroItens = 3;
    private static int numeroTransacoes = 4;
    private static int numeroAcessos = 9; //operações
    private static Scanner scanner;

    private static ArrayList<String> listaDeSchedule = new ArrayList<>();
    private static ArrayList<String> transacaoEscalonada = new ArrayList();
    private static ArrayList<String> listaEspera = new ArrayList();

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        System.out.println("Criando transacoes e gravando no banco...");
        Produtor produtor = new Produtor(numeroItens, numeroTransacoes, numeroAcessos);
        produtor.start();
        System.out.println("Pressione Enter para encerrar a producao!");

        if (scanner.hasNextLine()) {
            System.out.println("Producao encerrada");
            produtor.setFlag(false);
        }
        geraSaidaSchedule();

    }

    public static void geraSaidaSchedule() {
        Schedule s = null;
        s = TransacaoDao.buscarTransacoes(0, numeroTransacoes, numeroAcessos);
        //System.out.println(s.getScheduleInList().toString());

        int i = 0;
        while (!s.getScheduleInList().isEmpty()) {
            Operacao dadosSchedule = s.getScheduleInList().pop();
            int idOperacao = dadosSchedule.getIdOperacao();
            int indice = dadosSchedule.getIndice();
            Acesso operacao = dadosSchedule.getAcesso();
            String dado = dadosSchedule.getDado().getNome();

            montaSchedule(idOperacao, indice, operacao, dado);

            i++;
            //quando a lista tiver fazia ou nao puder continuar, deeve fazer uma nova consulta
        }

    }

    /*
        Método responsável por receber uma schedule do banco
        e montar em formato de schedule
     */
    public static void montaSchedule(int idOperacao, int indice, Acesso operacao, String dado) {
        String s = null;
        if (operacao.name().equals("S")) {
            s = "S" + indice;
        } else if (operacao.name().equals("E")) {
            s = "E" + indice;
        } else if (operacao.name().equals("R")) {
            s = "R" + indice + "(" + dado + ")";
        } else if (operacao.name().equals("W")) {
            s = "W" + indice + "(" + dado + ")";
        } else if (operacao.name().equals("C")) {
            s = "C" + indice;
        } else if (operacao.name().equals("A")) {
            s = "A" + indice;
        }

        listaDeSchedule.add(s);
        escalonarSchedule();
    }

    /*
        
        Método responsável por processar uma lista de schedule
        e escalonar as operações
     */
    public static void escalonarSchedule() {
        
        for (int i = 0; i < listaDeSchedule.size(); i++) {
            
            if (listaDeSchedule.get(i).contains("S")) {
                transacaoEscalonada.add(listaDeSchedule.get(i));

            } else if (listaDeSchedule.get(i).contains("E")) {
                transacaoEscalonada.add(listaDeSchedule.get(i));

            } else if (listaDeSchedule.get(i).contains("W")) {
                String[] splitOperacao = listaDeSchedule.get(i).split("\\(");
                String apenasDado = splitOperacao[1];
                splitOperacao = apenasDado.split("\\)");
                apenasDado = splitOperacao[0];


                //verifica se alguem já está lendo
                boolean igualdadeDado = verificaDadosIguais(listaDeSchedule.get(i),"W", apenasDado, i);
                
                if (igualdadeDado == true) {
                    boolean opJaTerminou = encontraOp(listaDeSchedule.get(i), "W", apenasDado, i);
                    System.out.println("PODE?? "+opJaTerminou);

                } else {
                    transacaoEscalonada.add(listaDeSchedule.get(i));
                }

            }

        }//for

    }

    /*
    Método responsável por verificar se um item do Schedule já existe
    na lista de Schedule.
    */
    public static boolean verificaDadosIguais(String transacao, String operacao, String dado, int indiceTransacao){
        boolean igualdadeDado = false;
        
        for (int i = 0; i < listaDeSchedule.size(); i++) {
            String schedule = listaDeSchedule.get(i);
            if(schedule.contains(operacao) && schedule.contains(dado) && i!=indiceTransacao){
                igualdadeDado = true;
            }
        }
        
        return igualdadeDado;
    }
    
    /*
        Método responsável por verificar se uma operação do schedule
        pode ser escalonada ou se já está sendo executada por outra operacao.
        Se pode, é adicionada na lista de operações escalonadas
        Se não, é adicionada na lista de espera.
     */
    public static boolean encontraOp(String transacao, String operacao, String dado, int posValor) {
        boolean operacaoExecutando = false;
        boolean opJaTerminou = false;
        
        String indiceOp = null;        
        for (int i = 0; i < listaDeSchedule.size(); i++) {
            if (listaDeSchedule.get(i).contains(valor) && i != posValor) {
                operacaoExecutando = true;
                String[] splitIndice = listaDeSchedule.get(i).split("()");
                indiceOp = splitIndice[1];

            }
        }

        //verifica se a operacao terminou
        if (operacaoExecutando == true) {
            String endOp = "E"+indiceOp;
            for (String ls : listaDeSchedule) {
                if (ls.equals(endOp)) {
                    opJaTerminou = true;
                    transacaoEscalonada.add(transacao);
                }
                else {
                    listaEspera.add(transacao);
                }
            }
        }

        return opJaTerminou;

    }

}
