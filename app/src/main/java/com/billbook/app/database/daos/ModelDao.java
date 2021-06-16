package com.billbook.app.database.daos;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Model;
import java.util.List;
import static androidx.room.OnConflictStrategy.REPLACE;
@Dao
public interface ModelDao {

    @Query("SELECT distinct * FROM Model WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Model>> getAllModels();

    @Query("SELECT * FROM Model WHERE id =:categoryId")
    Category getAllModelById(int categoryId);

    @Insert(onConflict = REPLACE)
    long insertModel(Model model);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Model> models);

    @Query("DELETE FROM Model")
    void deleteAll();

}
