package dk.kb.cumulus.repository;

import dk.kb.cumulus.model.Image;
import dk.kb.cumulus.model.ImageWord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by dgj on 05-03-2018.
 */
@Repository
public class ImageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Image getImage(int id){
        List<Image> rs = queryForImages("SELECT id,path,cumulus_id,category,color,ocr,status FROM images "+
                            "WHERE id='"+id+"'");
        if (rs.size() > 0) {
            return rs.get(0);
        } else {
            return null;
        }
    };

    public List<Image> listAllImages() {
        return queryForImages("SELECT id,path,cumulus_id,category,color,ocr,status FROM images");
    }

    public List<Image> listImagesInCategory(String category) {
        return queryForImages("SELECT id,path,cumulus_id,category,color,ocr,status " +
                "FROM images WHERE category ='"+category+"'");
    }

    public List<Image> listImagesInCategoryWithStatus(String category, String status) {
        return queryForImages("SELECT id,path,cumulus_id,category,color,ocr,status " +
                "FROM images WHERE category = '"+category+"' "+
                "AND status = '"+status+"'");
    }

    public int createImage(Image img) {
        final String sql = "INSERT INTO images (path,cumulus_id,category,status) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement pst =
                                con.prepareStatement(sql, new String[] {"id"});
                        pst.setString(1, img.getPath());
                        pst.setString(2, img.getCumulus_id());
                        pst.setString(3, img.getCategory());
                        pst.setString(4, img.getStatus());
                        return pst;
                    }
                },
                keyHolder);
        return (int)keyHolder.getKey();
    }

    public void updateImage(Image img)  {
        jdbcTemplate.update(
                "UPDATE images SET (path,cumulus_id,category,status) = (?,?,?,?) WHERE id = ?",
                img.getPath(),img.getCumulus_id(),img.getCategory(),img.getStatus(),img.getId());
    }

    public void createImageWord(ImageWord imageWord) {

    }

    private List<Image> queryForImages(String sql) {
        return jdbcTemplate.query(sql,
                (rs,rowNum) -> new Image(rs.getInt("id"), rs.getString("path"),
                rs.getString("cumulus_id"), rs.getString("category"),
                rs.getString("color"),rs.getString("ocr"),rs.getString("status")));
    }

}