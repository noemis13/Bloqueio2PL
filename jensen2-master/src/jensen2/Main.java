package jensen2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import static jensen2.TransacaoDao.adicionaListaEscalonadaNoBanco;

public class Main {

    private static int numeroItens = 3;
    private static int numeroTransacoes = 4;
    private static int numeroAcessos = 9; //operações
    private static Scanner scanner;

    private static ArrayList<String> listaDeSchedule = new ArrayList<>();
    private static ArrayList<String> transacaoEscalonada = new ArrayList();
    private static ArrayList<String> listaEspera = new ArrayList();
    private static ArrayList<String> transacoesAbortadas = new ArrayList<>();

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
        //Ao final do escalonamento, executa as transações que estão esperando
        executaListaEspera();
        //adicionar as transacoes abortadas
        executaTransacoesAbortadas();
        
        //adicionar transações escalonadas no banco de dados
        adicionaOperacoesEscalonadas();
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
            //verifica se existe um deadLock
            String indiceOp = null;
            String[] indice = listaDeSchedule.get(i).split("");
            indiceOp = indice[1];

            verificaDeadLock(transacao, indiceOp, i);

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

        } else if (transacao.contains("R")) {
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

        } else if (transacao.contains("C")) {
            String indiceOp = null;
            String[] indice = listaDeSchedule.get(i).split("");
            indiceOp = indice[1];
            
            //deve verificar se a transacao do C foi abortada
            for (int j = 0; j < transacoesAbortadas.size(); j++) {
                if(transacoesAbortadas.get(j).contains(indiceOp)){
                   transacoesAbortadas.add(transacao);
                }else {
                    transacaoEscalonada.add(transacao);
                }
            }
            
        }//if
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

    
    /*
    Método responsável por verificar se a transação
    está em deadLock
     */
    public static void verificaDeadLock(String transacao, String indiceOperacao, int cont) {
        boolean aborta = false;
        String transcaoAbort = null;

        for (int i = 0; i < transacaoEscalonada.size(); i++) {
            //verificar se existe algum transacao em espera
            if (!transacaoEscalonada.get(i).startsWith("S")) {
                if (!transacaoEscalonada.get(i).startsWith("E")) {
                    if (!transacaoEscalonada.get(i).contains("C") && transacaoEscalonada.get(i).contains(indiceOperacao)) {
                        String[] split = transacaoEscalonada.get(i).split("\\(");
                        String dado = split[1];
                        String[] apenasDado = dado.split("\\)");
                        dado = apenasDado[0];

                        for (int j = 0; j < listaEspera.size(); j++) {
                            if (listaEspera.get(j).contains(dado) && !listaEspera.get(j).contains(indiceOperacao)) {
                                //verifica se existe deadLock
                                aborta = verificaSeTemW(indiceOperacao, dado, cont);
                                if (aborta == true) {
                                    transcaoAbort = transacaoEscalonada.get(i);
                                }
                            }
                        }

                    }//if
                }//if
            }//if
        }//for

        //aborta transacao se nescessário.
        if (aborta == true) {
            String start = "S"+indiceOperacao;
            transacaoEscalonada.remove(start);
            transacaoEscalonada.remove(transcaoAbort);
            
            transacoesAbortadas.add(start);
            transacoesAbortadas.add(transcaoAbort);
            
            //verifica se tem um commit
            for (int i = 0; i < transacaoEscalonada.size(); i++) {
                if(transacaoEscalonada.get(i).startsWith("C") && transacaoEscalonada.get(i).contains(indiceOperacao)){
                    transacaoEscalonada.remove(transacaoEscalonada.get(i));
                    transacoesAbortadas.add(transacaoEscalonada.get(i));
                }
            }
            
        } else {
            transacaoEscalonada.add(transacao);
        }
        
    }

    /*
    Método responsável por dado uma transacao que está terminando
    Verifica, se ela além de realizar um READ, fez um WRITE
     */
    public static boolean verificaSeTemW(String indiceOperacao, String dado, int cont) {
        boolean aborta = true;
        for (int i = 0; i < transacaoEscalonada.size(); i++) {
            String write = "W" + indiceOperacao + "(" + dado + ")";
            if (transacaoEscalonada.get(i).equals(write) && i < cont) {
                aborta = false;
                break;
            }
        }

        return aborta;
    }

    
    /*
    Método responsável por verificar a lista de esperar
    e executar. Verifica se pode ser escalonada ou se existe um deadLock
     */
    public static void executaListaEspera() {
        boolean removeEexecuta = false;

        for (int i = 0; i < listaEspera.size(); i++) {
            String[] splitOperacao = listaEspera.get(i).split("\\(");
            String apenasDado = splitOperacao[1];
            splitOperacao = apenasDado.split("\\)");
            apenasDado = splitOperacao[0];

            String[] splitIndice = listaEspera.get(i).split("");
            String indice = splitIndice[1];

            for (int j = 0; j < transacaoEscalonada.size(); j++) {
                if (transacaoEscalonada.get(j).contains("E") || transacaoEscalonada.get(j).contains("C")
                        && !transacaoEscalonada.get(j).contains(indice) && transacaoEscalonada.get(j).contains(apenasDado)) {
                    removeEexecuta = true;
                    transacaoEscalonada.add(listaEspera.get(i));
                    
                }

            }//for escalonado
            if (removeEexecuta == true) {
                listaEspera.remove(i);
                
            }

        }//for espera
    }

    /*
    Método responsável por adicionar ao final da
    lista de escalonamento as trasações
    que foram abortadas devido ao deadLock.
    */
    public static void executaTransacoesAbortadas(){
        for (int i = 0; i < transacoesAbortadas.size(); i++) {
            transacaoEscalonada.add(transacoesAbortadas.get(i));
        }
    }

    
    /*
        Método responsável por adicionar as transacaoEscalonada
        na operacoes escalonadas que será adicionado no banco
     */
    private static void adicionaOperacoesEscalonadas() {
        
        for (int i = 0; i < transacaoEscalonada.size(); i++) {
            String[] split = transacaoEscalonada.get(i).split("");
            String acesso = split[0];
            int indice = Integer.parseInt(split[1]);

            if (acesso.equals("W") || acesso.equals("R")) {
                String[] s = transacaoEscalonada.get(i).split("\\(");
                String dado = s[1];
                String[] apenasDado = dado.split("\\)");
                dado = apenasDado[0];
                
                adicionaListaEscalonadaNoBanco(indice, dado, dado);
            /*} else{
                adicionaListaEscalonadaNoBanco(indice, acesso, null);
            */}
        
        }
    }

}
