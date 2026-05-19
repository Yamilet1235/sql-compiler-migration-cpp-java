package com.sqlcompiler.domain.semantic;

import com.sqlcompiler.domain.model.Table;
import com.sqlcompiler.domain.model.DataType;
import com.sqlcompiler.domain.port.SchemaRepositoryPort;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final SchemaRepositoryPort schemaRepository;
    // Almacenamiento local temporal en memoria para el compilador dinámico
    private final Map<String, Table> tablesInMemory;

    public SymbolTable(SchemaRepositoryPort schemaRepository) {
        this.schemaRepository = schemaRepository;
        this.tablesInMemory = new HashMap<>();
    }

    public Table findTable(String tableName) {
        if (tableName == null) return null;
        
        String cleanName = tableName.trim().toLowerCase();

        // 1. Intentar buscar en la base de datos temporal en memoria
        if (tablesInMemory.containsKey(cleanName)) {
            return tablesInMemory.get(cleanName);
        }

        // 2. Si no está y existe un repositorio real, buscar allá
        if (schemaRepository != null) {
            return schemaRepository.findTable(tableName);
        }

        return null;
    }

    public void loadSchema(String jsonSchema) {
        // Ejecutar comportamiento por defecto si el adaptador existe
        if (schemaRepository != null) {
            schemaRepository.loadSchema(jsonSchema);
        }

        // Carga y procesamiento local del JSON estructurado proveniente del frontend
        if (jsonSchema == null || jsonSchema.isBlank()) {
            return;
        }

        try {
            JSONObject root = new JSONObject(jsonSchema);
            if (root.has("tablas")) {
                JSONArray tablasArray = root.getJSONArray("tablas");
                
                for (int i = 0; i < tablasArray.length(); i++) {
                    JSONObject tablaObj = tablasArray.getJSONObject(i);
                    String nombreTabla = tablaObj.getString("nombre");
                    
                    // Creamos la instancia de nuestro modelo de dominio
                    Table nuevaTabla = new Table(nombreTabla);
                    JSONArray columnasArray = tablaObj.getJSONArray("columnas");
                    
                    for (int j = 0; j < columnasArray.length(); j++) {
                        String nombreColumna = columnasArray.getString(j);
                        // Por defecto asignamos INT o VARCHAR genérico, el analizador validará existencia de nombres
                        nuevaTabla.addColumn(nombreColumna, DataType.VARCHAR);
                    }
                    
                    // Guardamos en memoria con llave en minúsculas para evitar problemas de mayúsculas/minúsculas
                    this.tablesInMemory.put(nombreTabla.toLowerCase(), nuevaTabla);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al parsear el esquema en la Tabla de Símbolos: " + e.getMessage());
        }
    }
}