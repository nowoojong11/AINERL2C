package Main;

import Entity.dupResultVo;
import Method.Method_dup;
import lombok.SneakyThrows;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
/*
* @TODO 추가 쓰레드문이 필요할 때 구현 (지금은 람다식으로 구현되있음)
*
* */
class finalRunnable implements Runnable {

    List<dupResultVo> vo_ext;
    List<dupResultVo> vo_entry;
    Iterator<dupResultVo> finaliter_extractor;
    Connection con;
    Method_dup Md;
    String sql_update;

    public finalRunnable(List<dupResultVo> vo_ext, List<dupResultVo> vo_entry, Iterator<dupResultVo> finaliter_extractor
            , Connection con, Method_dup Md, String sql_update) {
        this.vo_ext = vo_ext;
        this.vo_entry = vo_entry;
        this.finaliter_extractor = finaliter_extractor;
        this.con = con;
        this.Md = Md;
        this.sql_update = sql_update;
    }


    @Override
    public void run() {
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
}