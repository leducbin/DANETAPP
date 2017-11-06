package com.movideo.baracus.clientimpl;

import com.movideo.baracus.model.product.Product;

import java.util.HashMap;
import java.util.Map;

public class ProductRepo {
    private ProductRepo(){

    }

    private static ProductRepo _instance;
    public static ProductRepo instance(){
        if (_instance == null)
            _instance = new ProductRepo();
        return _instance;
    }

    private Map<Integer, Product> products = new HashMap<Integer, Product>();

    public void saveProduct(Product product){
        if (!products.containsKey(product.getId())){
            products.put(product.getId(), product);
        }
    }

    public Product getProduct(int productId){
        if (products.containsKey(productId)){
            return products.get(productId);
        }
        else {
            return null;
        }
    }


}
