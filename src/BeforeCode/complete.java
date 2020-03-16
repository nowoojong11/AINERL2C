//package Main;
//
//import Entity.dupResultVo;
//import Method.Method_dup;
//
//import javax.print.DocFlavor;
//import java.io.FileReader;
//import java.sql.*;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//
//public class complete {
//    public static void main(String args[]) throws Exception {
//        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기
//        Method_dup Md = new Method_dup();
//        Connection con = Md.dbcon_parser();
//
//
//        /*
//        * sql 명령문을 파일에서 읽어 옵니다.
//        * ex) sql_entry_ext = SELECT * FROM ~~
//        * */
//
//        Map<String,String> query_list = Md.read_sqlquery("target");
//        String sql_filesize_ext     = query_list.get("sql_filesize_ext");
//        String sql_entry_ext        = query_list.get("sql_entry_ext");
//        String sql_update           = query_list.get("sql_update");
//
//        /*
//        * 데이터 베이스 서버에서 명령문으로 호출 합니다.
//        * rs_size_ext는 파일 사이즈가 중복인 로우들을 가져옵니다
//        * rs_entry_ext는 전체 로우를 호출합니다.
//        * */
//
//
//        ResultSet rs_size_ext =  con.createStatement().executeQuery(sql_filesize_ext);
//        ResultSet rs_entry_ext = con.createStatement().executeQuery(sql_entry_ext);
//
////        List<dupResultVo> vo_ext ;
////        vo_ext = Md.text_check_refac(rs_size_ext);
////        List<dupResultVo> vo_entry ;
//
////        ExecutorService newp = Executors.newFixedThreadPool(8);
////        try{
////            Runnable r = new Runnable(vo_ext,)
////        }
//
//
//
//        /*
//        * 쓰레드풀 호출부 입니다.
//        * */
//
////        ExecutorService p = Executors.newFixedThreadPool(Integer.parseInt(Md.read_env("core", "env")));
//        ExecutorService p = Executors.newSingleThreadExecutor();
//        try {
//            Runnable r = new multiRunnalbe_refac(sql_update, rs_size_ext, rs_entry_ext, con);
//            p.execute(r);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//
//        while(!p.awaitTermination(1, TimeUnit.SECONDS)){
//            System.out.println((System.currentTimeMillis()-beforeTime )/1000 + "seconds");
//            System.out.println(p.isTerminated());
//            if(p.isTerminated()) {
//                System.out.println("isTerminated가 맞음");
//            }else if(p.isShutdown()){
//                System.out.println("isshutdown이 맞음");
//            }
//            p.shutdown();
//
//        }
//
//
//    }
//}
//
