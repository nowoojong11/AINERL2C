package duplication_test;

import Method.Method_dup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class compare_dup {
    public static void main(String args[]) throws IOException, SQLException {
        Method_dup md = new Method_dup();
        Connection con = md.dbcon_parser();

        Map<String, String> whatis = md.read_sqlquery("com");

        /*
        * sql_text_list는 중복추출한 Path 리스트 중 Text를 가져옵니다.
        * sql_duppath_list는 중복추출한 Path 리스트 중 DupPath를 가져옵니다.
        * */
        String sql_text_list = whatis.get("sql_compare_text");
        String sql_duppath_list = whatis.get("sql_compare_duppath");

        /*
        * HashMap형태로 저장합니다.
        * */
        Map<String, String> entry_list = compare_entry(con,  sql_text_list);
        Map<String, String> dup_list = compare_dup(con, sql_duppath_list);


        /*
        * dupPath와 fulPath를 비교하여 text가 틀리면
        * 건수를 보여줍니다.
        * */
        dup_list.forEach((fulpath, duppath) -> {
                    String[] dup_arr = duppath.split("::");

                    for (int i = 0; i < dup_arr.length; i++) {

                        String dup_text = entry_list.get(dup_arr[i].trim());
                        String ful_text = entry_list.get(fulpath);

                        if (!dup_text.equals(ful_text)) {
                            System.out.println("틀린 dup text는 : " + dup_text);
                            System.out.println("틀린 ful text는 : " + ful_text);
                        }
                    }
                });
    }

    /*
    * DB에서 중복추출한 건수 중에서
    * FILE_FULL_PATH와 FILE_TEXT를 가져옵니다.
    * */
    static Map<String, String> compare_entry(Connection con, String sql_query) throws SQLException {
        ResultSet rs_perf = con.createStatement().executeQuery(sql_query);
        Map<String, String> fin_arr = new HashMap<>();
        while (rs_perf.next()) {
            String FILE_FULL_PATH = rs_perf.getString("FILE_FULL_PATH");
            String FILE_TEXT =  rs_perf.getString("FILE_TEXT");

            fin_arr.put(FILE_FULL_PATH, FILE_TEXT);
        }
        return fin_arr;
    }


    /*
     * DB에서 중복추출한 건수 중에서
     * FILE_FULL_PATH와 FILE_DUP_PATH를 가져옵니다.
     * */
    static Map<String, String> compare_dup(Connection con, String sql_query) throws SQLException {
        ResultSet rs_perf = con.createStatement().executeQuery(sql_query);
        Map<String, String> fin_arr = new HashMap<>();


        while (rs_perf.next()) {
            String FILE_FULL_PATH = rs_perf.getString("FILE_FULL_PATH");
            String FILE_DUP_PATH = rs_perf.getString("FILE_DUP_PATH");

            fin_arr.put(FILE_FULL_PATH, FILE_DUP_PATH);
        }
        return fin_arr;
    }
}
