package jensen2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import static jensen2.TransacaoDao.adicionaListaEscalonadaNoBanco;

public class Schedule {

    private LinkedList<Operacao> scheduleInList;
    private LinkedList<Operacao> operacoesEscalonadas;

    public Schedule(LinkedList<Transacao> transacoes) {
        scheduleInList = new LinkedList<>();
        ligaOperacoes(transacoes);
    }

    public Schedule() {
    }

    private void ligaOperacoes(LinkedList<Transacao> transacoes) {
        Random r = new Random();
        while (!transacoes.isEmpty()) {
            int n = r.nextInt(transacoes.size());
            if (!transacoes.get(n).transIsEmpty()) {
                scheduleInList.add(transacoes.get(n).getFirstOp());
                transacoes.get(n).removeOp();
            } else {
                transacoes.remove(n);
            }
        }
    }

    
    public LinkedList<Operacao> getScheduleInList() {
        return scheduleInList;
    }

    public void setScheduleInList(LinkedList<Operacao> scheduleInList) {
        this.scheduleInList = scheduleInList;
    }

    public LinkedList<Operacao> getOperacoesEscalonadas() {
        return operacoesEscalonadas;
    }

    public void setOperacoesEscalonadas(LinkedList<Operacao> operacoesEscalonadas) {
        this.operacoesEscalonadas = operacoesEscalonadas;
    }

}
