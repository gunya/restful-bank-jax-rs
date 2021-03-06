package com.hepexta.jaxrs.bank.repository.db;

import com.hepexta.jaxrs.bank.ex.ErrorMessage;
import com.hepexta.jaxrs.bank.ex.TransferException;
import com.hepexta.jaxrs.bank.model.Client;
import com.hepexta.jaxrs.bank.repository.Repository;
import com.hepexta.jaxrs.bank.repository.dao.mapper.ClientMapper;
import com.hepexta.jaxrs.bank.repository.dao.mapper.ResultSetMapper;
import com.hepexta.jaxrs.util.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.hepexta.jaxrs.bank.repository.db.Queries.QUERY_CBS_CLIENT_DELETE;
import static com.hepexta.jaxrs.bank.repository.db.Queries.QUERY_CBS_CLIENT_FIND_BY_ID;
import static com.hepexta.jaxrs.bank.repository.db.Queries.QUERY_CBS_CLIENT_GET_LIST;
import static com.hepexta.jaxrs.bank.repository.db.Queries.QUERY_CBS_CLIENT_INSERT;
import static com.hepexta.jaxrs.bank.repository.db.Queries.QUERY_CBS_CLIENT_UPDATE;
import static com.hepexta.jaxrs.util.DBUtils.getId;

public class ClientRepositoryDBImpl implements Repository<Client> {

    private static final Logger LOG = LoggerFactory.getLogger(ClientRepositoryDBImpl.class);
    private ResultSetMapper<Client> mapper = new ClientMapper();
    private static ClientRepositoryDBImpl instance;

    private ClientRepositoryDBImpl() {
    }

    public static ClientRepositoryDBImpl getInstance(){
        if (instance ==null){
            instance = new ClientRepositoryDBImpl();
        }
        return instance;
    }

    @Override
    public List<Client> getList() {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(QUERY_CBS_CLIENT_GET_LIST);
             ResultSet resultSet =  stmt.executeQuery()){
            while (resultSet.next()) {
                clients.add(mapper.map(resultSet));
            }
        } catch (SQLException e) {
            LOG.error("Error getting data", e);
        }
        return clients;
    }

    @Override
    public Client findById(String id) {
        Client clients = null;
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = prepareFindByIdStmnt(conn, id);
             ResultSet resultSet =  stmt.executeQuery()){
            if (resultSet.next()) {
                clients = mapper.map(resultSet);
            }
        } catch (SQLException e) {
            LOG.error("Error getting data", e);
        }
        return clients;
    }

    private PreparedStatement prepareFindByIdStmnt(Connection conn, String id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(QUERY_CBS_CLIENT_FIND_BY_ID);
        stmt.setString(1, id);
        return stmt;
    }

    @Override
    public String insert(Client model) {
        String result;
        try (Connection conn = DBUtils.getConnection(); PreparedStatement stmt = conn.prepareStatement(QUERY_CBS_CLIENT_INSERT, Statement.RETURN_GENERATED_KEYS)){
            stmt.setString(1, model.getName());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOG.error("Error inserting client {}", model);
                throw new TransferException(ErrorMessage.ERROR_526);
            }
            result = getId(stmt);
        } catch (SQLException e) {
            throw new TransferException(ErrorMessage.ERROR_526, e);
        }

        return result;
    }

    @Override
    public boolean modify(Client...model) {
        boolean result = true;
        try (Connection conn = DBUtils.getConnection(); PreparedStatement stmt = conn.prepareStatement(QUERY_CBS_CLIENT_UPDATE)){
            for (Client client : model) {
                stmt.setString(1, client.getName());
                stmt.setString(2, client.getId());
                result &= stmt.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            LOG.error("Error updating client", e);
        }
        return result;
    }

    @Override
    public boolean delete(String id) {
        boolean result = false;
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement stmt = prepareDeleteStmnt(conn, id)){
            result = stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            LOG.error("Error deleting client", e);
        }
        return result;
    }

    private PreparedStatement prepareDeleteStmnt(Connection conn, String id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(QUERY_CBS_CLIENT_DELETE);
        stmt.setString(1, id);
        return stmt;
    }

}
