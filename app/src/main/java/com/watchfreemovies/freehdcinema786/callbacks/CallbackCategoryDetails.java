package com.watchfreemovies.freehdcinema786.callbacks;

import com.watchfreemovies.freehdcinema786.models.Category;
import com.watchfreemovies.freehdcinema786.models.News;

import java.util.ArrayList;
import java.util.List;

public class CallbackCategoryDetails {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public Category category = null;
    public List<News> posts = new ArrayList<>();

}
