package Method;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;

import Entity.dupResultVo;
import kr.datasolution.dsnlp.nlp.modules.segment.Segmenter;
import kr.datasolution.dsnlp.nlp.modules.segment.config.SegmentConfig;

public class Method_dup {

    public void update_duplication_text(List<dupResultVo> vo_ext, List<dupResultVo> vo_entry, Iterator<dupResultVo> finaliter_extractor
            , Connection con,String sql_update){
    String more_than_entry_path = "", input_fullpath = "", input_size = "";
        try {
        boolean check = false;
        int ext_count = 0;
        int entry_count = 0;

        /*
         * 사이즈 중복추출한 iterator를 기준으로 loop 실행합니다.
         * */
        while (finaliter_extractor.hasNext()) {

            entry_count = 0;

            /*
             * entry iterator를 기준으로 loop을 수행한 변수들을 저장한뒤
             * insert 쿼리문을 실행합니다.
             * */
            if (!more_than_entry_path.equals("")) {
                update_query(con, sql_update, input_fullpath, more_than_entry_path, input_size);
                input_fullpath = "";
                input_size = "";
                more_than_entry_path = "";
                check = !check;
            }


            String extractor_path = finaliter_extractor.next().getFullpath();
            String extractor_text = vo_ext.get(ext_count).getText();
            String extractor_size = vo_ext.get(ext_count).getFilesizekb();
            Iterator<dupResultVo> finaliter_entry = vo_entry.iterator();
            ext_count += 1;

            /*
             * 사이즈 기준 중복추출 로우를 전체 로우와 비교합니다.
             * */
            while (finaliter_entry.hasNext()) {
                String entry_path = finaliter_entry.next().getFullpath();
                String entry_text = vo_entry.get(entry_count).getText();
                String entry_size = vo_entry.get(entry_count).getFilesizekb();
                /*
                 * 기준
                 * 1. text 중복 여부
                 * 2. PK 고려
                 * 3. text 중복이지만 사이즈가 다를 경우.
                 * */

                if (extractor_text.equals(entry_text)
                        && (!extractor_path.equals(entry_path)) && (entry_size.equals(extractor_size))) {

                    input_fullpath = extractor_path;
                    input_size = extractor_size;
                    /*
                     * 추출이 1일 경우는 단순 필드 선언
                     * 추출이 2이상일 경우 String 합병.
                     * */
                    if (!check) {
                        more_than_entry_path = entry_path;
                        check = !check;
                    } else {
                        more_than_entry_path += "::" + entry_path;
                    }

                }
                entry_count += 1;
            }
        }
    }catch(SQLException sqle){
        sqle.printStackTrace();
    }
}
    /*
    * 조건에 만족한 로우를 DB에 업데이트 시킵니다
    * input_fullpath        = PK가 되는 파일 경로
    * more_than_entry_path  = PK와 중복이 되는 경로들
    * filesize              = PK의 파일 사이즈
    * */
    private void update_query(Connection con, String sql_update, String input_fullpath,
                              String more_than_entry_path, String filesize) throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(sql_update);
        pstmt.setString(1, input_fullpath);
        pstmt.setString(2, more_than_entry_path);
        pstmt.setString(3, input_fullpath.substring(input_fullpath.lastIndexOf("\\") + 1));
        pstmt.setInt(4, Integer.parseInt(filesize));


        int result_update = pstmt.executeUpdate();
        System.out.println(result_update);
    }

    /*
    * query를 호출 한 뒤  받아온 ResultSet을ㄹ
    * list에 저장시킵니다.
    * */
    public List<dupResultVo>  text_check_test(ResultSet rs) throws SQLException, NoSuchAlgorithmException {
        List<dupResultVo> list_dRV = new ArrayList();
        while (rs.next()) {
            String path = rs.getString("FILE_FULL_PATH");
            String text = HashSHA(rs.getString("FILE_TEXT"));
            String size = rs.getString(("FILE_SIZE"));
            list_dRV.add(new dupResultVo(path, size, text));
        }
        System.out.println(list_dRV.toString());
        return list_dRV;
    }


    /*
    * mariadb에 저장되어있는 DB 서버에 연결합니다.
    * */

    public Connection dbcon_parser() {
        String driver = "org.mariadb.jdbc.Driver";
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection("jdbc:mariadb://192.168.210.119:3307/news?autoReconnect=true", "ainer", "ainer#123");
            if (con != null) {
                System.out.println("DB 접속 성공");
            }

            return con;
        } catch (ClassNotFoundException | SQLException var4) {
            var4.printStackTrace();
            return null;
        }
    }
    /*
    * sql 파일중에서 원하는 쿼리문을 가져옵니다.
    *
    * */
    public Map<String, String> read_sqlquery(String sqlName) throws IOException {
        BufferedReader BR = new BufferedReader(new FileReader(new File(".\\src\\sqlsource\\SQL_QUERY_" + sqlName.toUpperCase() + ".file")));
        Map<String, String> sql_list = new HashMap<>();
        String line;
        while ((line = BR.readLine()) != null) {
            sql_list.put(line.split(":")[0], line.split(":")[1]);
        }
        return sql_list;
    }

    /*
    * env파일에 있는 환경변수들을 가져옵니다.
    * */
    public String read_env(String text, String file) throws IOException {
        BufferedReader BR = new BufferedReader(new FileReader(new File(".\\src\\" + file)));
        String line;
        while ((line = BR.readLine()) != null) {
            if (line.split(":")[0].contains(text)) {
                return line.split(":")[1];
            }
        }
        return null;
    }


    public String segment_text(String text) throws IOException {
        SegmentConfig segmentConfig = new SegmentConfig();
        segmentConfig.setDictPath(read_env("ngramProp", "env"));
        Segmenter segmenter = new Segmenter(segmentConfig);

        String[] normTextList = segmenter.segment_sentences(text);
        StringBuilder builder = new StringBuilder();

        for (String norm : normTextList) {
            builder.append(norm);
        }
        return builder.toString();
    }

    /*
    * String message를 해쉬코드로 변환합니다.
    * */
    public static String HashSHA(String msg) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(msg.getBytes());
        StringBuilder builder = new StringBuilder();
        for (byte b : md.digest()) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

}