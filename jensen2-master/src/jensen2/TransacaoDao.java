package jensen2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class TransacaoDao {
	private static MinhaConexao minhaConexao;
	
	public TransacaoDao() {
		minhaConexao = new MinhaConexao();
		MinhaConexao.getCabecalho();
	}

	public static void gravarTransacoes(Schedule schedule) {
		Operacao operacao = null;
		
		Connection conn = minhaConexao.getConnection();
		String sql = "INSERT INTO schedule(indiceTransacao, operacao, itemDado, timestampj) VALUES (?, ?, ?, ?)";
		PreparedStatement stmt = null;
		while(!schedule.getScheduleInList().isEmpty()) {
			operacao = schedule.getScheduleInList().removeFirst();
			try {
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, operacao.getIndice());
				stmt.setString(2, operacao.getAcesso().toString());
				stmt.setString(3, operacao.getDado().getNome());
				stmt.setString(4, new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
				stmt.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Erro na insercao da transacao");
				e.printStackTrace();
			}
		}
		try {
			minhaConexao.releaseAll(stmt, conn);
		} catch (SQLException e) {
			System.out.println("Erro ao encerrar conex�o");
			e.printStackTrace();
		}
	}
        
        //Busca no Banco
        public static Schedule buscarTransacoes(int idoperacao, int transacoes, int operacoes){
            Schedule s = null;
            LinkedList<Operacao> scheduleInList = new LinkedList<>();
            
            Connection con = minhaConexao.getConnection();
            String sql = "SELECT * "
                    + "FROM schedule s "
                    + "WHERE s.idoperacao >" + idoperacao + "AND s.idoperacao<=" 
                    + idoperacao + (transacoes * operacoes) + (transacoes * 2);
            
            try {
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    Operacao operacao = new Operacao(
                            rs.getInt("idoperacao"),
                            rs.getString("itemdado"),
                            Acesso.valueOf(rs.getString("operacao").toUpperCase()),
                            rs.getInt("indicetransacao")
                            );
                    scheduleInList.add(operacao);
                }
                s = new Schedule();
                s.setScheduleInList(scheduleInList);
                minhaConexao.releaseAll(rs, ps, con);
            
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return s;        
        }
	
        
	public static int pegarUltimoIndice() {
		int ultimoIndice = 0;
		minhaConexao = new MinhaConexao();
		Connection conn = minhaConexao.getConnection();
		String sql = "SELECT MAX(indiceTransacao) FROM schedule;";
		
		try {
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			ultimoIndice = rs.getInt(1);
			
		} catch (SQLException e) {
			System.err.println("Erro na consulta ao �ltimo �ndice");
			e.printStackTrace();
		}
		return ultimoIndice;
	}
        

}
