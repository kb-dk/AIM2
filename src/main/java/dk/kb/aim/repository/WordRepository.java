package dk.kb.aim.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import dk.kb.aim.ImageStatus;
import dk.kb.aim.WordStatus;
import dk.kb.aim.model.Image;
import dk.kb.aim.model.Word;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by dgj on 22-02-2018.
 */
@Repository
public class WordRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public int createWord(Word word) {
        final String sql = "INSERT INTO words (text_en,text_da,category,status) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement pst =
                                con.prepareStatement(sql, new String[] {"id"});
                        pst.setString(1, word.getText_en());
                        pst.setString(2, word.getText_da());
                        pst.setString(3, word.getCategory());
                        pst.setString(4, word.getStatus().toString());
                        return pst;
                    }
                },
                keyHolder);
        return (int)keyHolder.getKey();
    }

    public Word getWord(int id){
        List<Word> rs = queryForWords("SELECT id,text_en,text_da,category,status FROM words "+
                "WHERE id='"+id+"'");
        if (rs.size() > 0) {
            return rs.get(0);
        } else {
            return null;
        }
    };

    public Word getWordByText(String text_en, String category) {
        List<Word> rs = queryForWords("SELECT id,text_en,text_da,category,status FROM words "+
                "WHERE text_en='"+text_en+"' AND category ='"+category+"'");
        if (rs.size() > 0) {
            return rs.get(0);
        } else {
            return null;
        }
    }


    public void updateWord(Word word)  {
        jdbcTemplate.update(
                "UPDATE words SET (text_en,text_da,category,status) = (?,?,?,?) WHERE id = ?",
                word.getText_en(),word.getText_da(),word.getCategory(),word.getStatus().toString(),word.getId());
    }

    public List<Word> allWords() {
        return queryForWords("SELECT id,text_en,text_da,category,status from words");
    }

    public List<Word> allWordsWithStatus(WordStatus status) {
        return queryForWords("SELECT id,text_en,text_da,category,status from words "+
                "WHERE status = '"+status+"'");
    }

    public List<Word> allWordsInCategory(String category) {
        return queryForWords("SELECT id,text_en,text_da,category,status from words "+
                "WHERE category = '"+category+"'");
    }

    public List<Word> allWordsInCategoryWithStatus(String category, WordStatus status) {
        return queryForWords("SELECT id,text_en,text_da,category,status from words "+
                "WHERE category = '"+category+"' "+
                "AND status = '"+status+"'");
    }

    public boolean isAcceptedFor(String text_en, String category) {
        String sql = "SELECT count(*) FROM words WHERE text_en = ? AND category = ? AND status = ?";
        int count = jdbcTemplate.queryForObject(sql, new Object[] {text_en, category, WordStatus.ACCEPTED.toString()}, Integer.class);
        return (count > 0);
    }

    public boolean isRejectedFor(String text_en, String category) {
        String sql = "SELECT count(*) FROM words WHERE text_en = ? AND category = ? AND status = ? ";
        int count = jdbcTemplate.queryForObject(sql, new Object[] {text_en, category, WordStatus.REJECTED.toString()}, Integer.class);
        return (count > 0);
    }

    public List<Word> getImageWords(int image_id,WordStatus status) {
        String sql = "SELECT * " +
                "from image_word i NATURAL JOIN words w WHERE " +
                "i.image_id = "+image_id+ " AND " +
                "w.status='"+status+"'";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Word(rs.getInt("id"), rs.getString("text_en"),
                        rs.getString("text_da"), rs.getString("category"),
                        WordStatus.valueOf(rs.getString("status"))));
    }

    public List<String> getCategories() {
        String sql = "select DISTINCT category from words";
        return jdbcTemplate.queryForList(sql,String.class);
    }

    private List<Word> queryForWords(String sql) {
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Word(rs.getInt("id"), rs.getString("text_en"),
                        rs.getString("text_da"), rs.getString("category"),
                        WordStatus.valueOf(rs.getString("status")))
                );
    }
}