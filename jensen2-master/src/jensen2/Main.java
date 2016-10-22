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

            String schedule = null;

            if (operacao.name().equals("S")) {
                schedule = "S" + indice;
            } else if (operacao.name().equals("E")) {
                schedule = "E" + indice;
            } else if (operacao.name().equals("R")) {
                schedule = "R" + indice + "(" + dado + ")";
            } else if (operacao.name().equals("W")) {
                schedule = "W" + indice + "(" + dado + ")";
            } else if (operacao.name().equals("C")) {
                schedule = "C" + indice;
            } else if (operacao.name().equals("A")) {
                schedule = "A" + indice;
            }

            listaDeSchedule.add(schedule);
            escalonarSchedule(i);
            
            i++;
            //quando a lista tiver fazia ou nao puder continuar, deeve fazer uma nova consulta
        }
    }

    /*
    Método responsável por processar uma lista de schedule
        e escalonar as operações
     */
    public static void escalonarSchedule(int i) {
        String transacao = listaDeSchedule.get(i);

        if (transacao.contains("S")) {
            transacaoEscalonada.add(transacao);

        } else if (transacao.contains("E")) {
            transacaoEscalonada.add(transacao);

        } else if (transacao.contains("C")) {
            transacaoEscalonada.add(transacao);

        } else if (transacao.contains("W")) {
            String[] splitOperacao = listaDeSchedule.get(i).split("\\(");
            String apenasDado = splitOperacao[1];
            splitOperacao = apenasDado.split("\\)");
            apenasDado = splitOperacao[0];

            String indiceOp = null;
            String operacao = null;
            String[] indice = listaDeSchedule.get(i).split("");
            indiceOp = indice[1];
            operacao = indice[0];

            boolean escalonada;
            boolean terminou;
            escalonada = verificaDadosIguais(transacao, indiceOp, apenasDado, i);
            realizaEscalonamento(escalonada, transacao);
            
        } else if (listaDeSchedule.get(i).contains("R")) {
            String[] splitOperacao = listaDeSchedule.get(i).split("\\(");
            String apenasDado = splitOperacao[1];
            splitOperacao = apenasDado.split("\\)");
            apenasDado = splitOperacao[0];

            String indiceOp = null;
            String operacao = null;
            String[] indice = listaDeSchedule.get(i).split("");
            indiceOp = indice[1];
            operacao = indice[0];

            boolean escalonada;
            boolean terminou;
            escalonada = verificaDadosIguais(transacao, indiceOp, apenasDado, i);
            realizaEscalonamento(escalonada, transacao);
        }
        
    }

    /*
    Método responsável por dado um dado da transação, 
    verificar se existe um read ou write do mesmo dado em uma outra
    transação diferente
     */
    public static boolean verificaDadosIguais(String transacao, String indiceOpTransacao, String apenasDadoTransacao, int iTransacao) {
        boolean escalona = false;

        for (int i = 0; i < iTransacao; i++) {
            //transacoes de mesmo dado com indices diferentes;
            if (!listaDeSchedule.get(i).contains(indiceOpTransacao) && listaDeSchedule.get(i).contains(apenasDadoTransacao)) {
                String[] split = listaDeSchedule.get(i).split("");
                String operecao = split[0];
                boolean verifica;
                        
                //verifica a operacao que possui o mesmo dado de indice diferente
                if (operecao.equals("W")) {
                    if (transacao.contains("R")) {
                        //verifica se existe um read do mesmo indice e dado
                        String readVerifica = "R" + indiceOpTransacao + "(" + apenasDadoTransacao + ")";
                        verifica = verificaReadMesmoIndice(readVerifica, iTransacao);
                        escalona = verificaTerminoTransacao(verifica, listaDeSchedule.get(i), iTransacao);
                        
                    } else {
                        verifica = false;
                        escalona = verificaTerminoTransacao(verifica, listaDeSchedule.get(i), iTransacao);
                        
                    }

                } else if (operecao.equals("R")) {
                    if (operecao.equals("W")) {
                        verifica = false;
                        escalona = verificaTerminoTransacao(verifica, listaDeSchedule.get(i), iTransacao);

                    } else {
                        escalona = true;
                    }
                }
            //a transacao pode ser escalonada    
            } else {
                escalona = true;
            }
        }//for
        return escalona;
    }

    /*
    Método responsável por verificar se existe um ReadX(dado) antes do WriteX(dado)
    retorna se a transação pode ser escalonada ou nao
     */
    public static boolean verificaReadMesmoIndice(String t, int posOperacao) {
        boolean escalonou = true;
        for (int i = 0; i < posOperacao; i++) {
            if (listaDeSchedule.get(i).equals(t)) {
                escalonou = true;
            } else {
                escalonou = false;
            }
        }
        return escalonou;
    }

    /*
    Método responsável por verificar
    se determinada transação já foi commitada ou terminada
     */
    public static boolean verificaTerminoTransacao(boolean escalonada, String transacao, int posOperacao) {
        boolean terminou = false;
        String[] indice = transacao.split("");
        String valorIndice = indice[1];

        String end = null;
        end = "E" + valorIndice;

        String commit = null;
        commit = "C" + valorIndice;

        if (escalonada == false) {
            for (int i = 0; i < posOperacao; i++) {
                if (listaDeSchedule.get(i).equals(end) || listaDeSchedule.get(i).equals(commit)) {
                    terminou = true;
                }

            }
        }

        return terminou;
    }


    /*
    Método resposável por adicionar transação na lista de escalonados
    ou adicionar na lista de espera
     */
    public static void realizaEscalonamento(boolean escalonou, String transacao) {
        if (escalonou == true) {
            transacaoEscalonada.add(transacao);

        } else {
            listaEspera.add(transacao);
        }
        
    }
}
