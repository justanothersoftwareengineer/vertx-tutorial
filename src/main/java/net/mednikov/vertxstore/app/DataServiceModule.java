package net.mednikov.vertxstore.app;

import com.google.inject.AbstractModule;
import com.mysql.cj.jdbc.MysqlDataSource;

import net.mednikov.vertxstore.repository.IProductRepository;
import net.mednikov.vertxstore.repository.MySQLProductRepository;

public class DataServiceModule extends AbstractModule {

    private IProductRepository productRepository;

    public DataServiceModule(String url){
        this.productRepository = new MySQLProductRepository(getMySqlDataSource(url));
    }

    private MysqlDataSource getMySqlDataSource(String jdbcUrl){
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(jdbcUrl);
        return dataSource;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(IProductRepository.class).toInstance(productRepository);
    }

    
}