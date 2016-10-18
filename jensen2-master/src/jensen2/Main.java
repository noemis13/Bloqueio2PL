package jensen2;

import java.util.Scanner;

public class Main {
	private static int numeroItens = 3;
	private static int numeroTransacoes = 4;
	private static int numeroAcessos = 9; //operações
	private static Scanner scanner;
	
	public static void main(String[] args) {
		scanner = new Scanner(System.in);
		System.out.println( "Criando transacoes e gravando no banco..." );
		Produtor produtor = new Produtor(numeroItens, numeroTransacoes, numeroAcessos);
		produtor.start();
		System.out.println("Pressione Enter para encerrar a producao!");
		
		if(scanner.hasNextLine()) {
			System.out.println("Producao encerrada");
			produtor.setFlag(false);
		}
                geraSaidaSchedule();
                               
		
	}
        
        public static void geraSaidaSchedule() {
            Schedule s = null;
            s = TransacaoDao.buscarTransacoes(0, numeroTransacoes, numeroAcessos);
            
            System.out.println(s.getScheduleInList().getFirst().toString());
            
        }
        

}
