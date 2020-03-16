package Main;

import Entity.dupResultVo;
import Method.Method_dup;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Refactoring {

    static List<dupResultVo> list_drv = new ArrayList<>();

    public static void main(String args[]) throws Exception {
        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기
        Method_dup Md = new Method_dup();
        int avaiableThreadNum = Integer.parseInt(Md.read_env("core", "env"));


        Connection con = Md.dbcon_parser();
        /*
         * sql 명령문을 파일에서 읽어 옵니다.
         * ex) sql_entry_ext = SELECT * FROM ~~
         * */

        Map<String, String> query_list = Md.read_sqlquery("insert");
        String sql_filesize_ext = query_list.get("sql_filesize_ext");
        String sql_entry_ext = query_list.get("sql_entry_ext");
        String sql_update = query_list.get("sql_update");

        /*
         * 데이터 베이스 서버에서 명령문으로 호출 합니다.
         * rs_size_ext는 파일 사이즈가 중복인 로우들을 가져옵니다
         * rs_entry_ext는 전체 로우를 호출합니다.
         * */


        ResultSet rs_size_ext = con.createStatement().executeQuery(sql_filesize_ext);
        ResultSet rs_entry_ext = con.createStatement().executeQuery(sql_entry_ext);
        ExecutorService newp = Executors.newFixedThreadPool(avaiableThreadNum);
        System.out.println(("DB조회 까지 걸린시간. " + (System.currentTimeMillis() - beforeTime) / 1000) + "seconds");

        /*
         * 추출물 텍스트 입니다.
         * */
        List<dupResultVo> vo_ext;
        vo_ext = Md.text_check_test(rs_size_ext);


        /*
         * entrySet을 dupResultVo의 List형식으로
         * 삽입합니다.(데이터 무결성과, 가장 많은 작업시간)
         * */

        while (rs_entry_ext.next()) {
            String path = rs_entry_ext.getString("FILE_FULL_PATH");
            String text = Md.HashSHA(rs_entry_ext.getString("FILE_TEXT"));
            String size = rs_entry_ext.getString(("FILE_SIZE"));
            newp.submit(() -> {
                Refactoring.list_drv.add(new dupResultVo(path, size, text));
            });
        }




        /*
         * update문이 실행됩니다.
         * 순서대로
         * 1.파일 사이즈가 동일
         * 2.파일 text가 동일
         * 3.조건이 만족한 갯수가 2개이상일 경우 ','으로 구분.
         * */
        while (!newp.awaitTermination(100, TimeUnit.MILLISECONDS)) {
            newp.shutdown();
            break;
        }


        Md.update_duplication_text(vo_ext, list_drv, vo_ext.iterator(), con, sql_update);
        System.out.println(("최종 걸린 시간은 " + (System.currentTimeMillis() - beforeTime) / 1000) + "seconds");

    }

}

