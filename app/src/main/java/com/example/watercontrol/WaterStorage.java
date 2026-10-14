package com.example.watercontrol;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterStorage {

    /*
     * Nome do arquivo de preferências utilizado para
     * armazenar os dados do aplicativo.
     */
    private static final String PREFS_NAME = "WaterControl";

    /*
     * Chaves utilizadas para identificar cada informação
     * armazenada nas SharedPreferences.
     */
    private static final String KEY_GOAL = "dailyGoal";
    private static final String KEY_DATE = "date";
    private static final String KEY_RECORDS = "records";
    private static final String KEY_ACCUMULATED = "accumulated";

    private final SharedPreferences preferences;

    public WaterStorage(Context context) {
        /*
         * SharedPreferences permite armazenar dados simples
         * de forma persistente mesmo após o fechamento do aplicativo.
         */
        preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    /*
     * Salva ou atualiza a meta diária do usuário.
     */
    public void saveGoal(int goal) {
        preferences.edit()
                .putInt(KEY_GOAL, goal)
                .apply();
    }

    /*
     * Recupera a meta diária armazenada.
     * Caso nenhuma meta tenha sido definida, utiliza 2000 ml
     * como valor padrão.
     */
    public int getGoal() {
        return preferences.getInt(
                KEY_GOAL,
                2000
        );
    }

    /*
     * Salva todos os registros do dia.
     *
     * Os registros são convertidos para JSON para que uma lista
     * de objetos WaterRecord possa ser armazenada dentro das
     * SharedPreferences.
     */
    public void saveRecords(List<WaterRecord> records) {

        JSONArray jsonArray = new JSONArray();

        int accumulated = 0;

        for (WaterRecord record : records) {

            JSONObject jsonObject = new JSONObject();

            try {
                /*
                 * Cada registro armazena o volume consumido
                 * e o horário em que o consumo foi registrado.
                 */
                jsonObject.put(
                        "volume",
                        record.getVolume()
                );

                jsonObject.put(
                        "timestamp",
                        record.getTimestamp()
                );

                jsonArray.put(jsonObject);

                /*
                 * O consumo acumulado é calculado a partir
                 * dos registros armazenados.
                 */
                accumulated += record.getVolume();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /*
         * Armazena os registros, o consumo acumulado e a data
         * correspondente aos registros.
         */
        preferences.edit()
                .putString(
                        KEY_RECORDS,
                        jsonArray.toString()
                )
                .putInt(
                        KEY_ACCUMULATED,
                        accumulated
                )
                .putString(
                        KEY_DATE,
                        getToday()
                )
                .apply();
    }

    /*
     * Recupera os registros armazenados.
     *
     * Antes de carregar os dados, verifica se eles pertencem
     * ao dia atual. Caso sejam de outro dia, os registros
     * anteriores são descartados.
     */
    public List<WaterRecord> getRecords() {

        List<WaterRecord> records = new ArrayList<>();

        String savedDate = preferences.getString(
                KEY_DATE,
                ""
        );

        if (!savedDate.equals(getToday())) {
            clearRecords();
            return records;
        }

        String json = preferences.getString(
                KEY_RECORDS,
                "[]"
        );

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject =
                        jsonArray.getJSONObject(i);

                int volume =
                        jsonObject.getInt("volume");

                long timestamp =
                        jsonObject.getLong("timestamp");

                records.add(
                        new WaterRecord(
                                volume,
                                timestamp
                        )
                );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return records;
    }

    /*
     * Recupera o consumo acumulado do dia.
     *
     * O valor armazenado é utilizado diretamente quando
     * disponível. Caso não exista, o acumulado é calculado
     * novamente a partir dos registros.
     */
    public int getAccumulatedConsumption() {

        String savedDate = preferences.getString(
                KEY_DATE,
                ""
        );

        /*
         * Impede que o consumo de um dia anterior seja
         * considerado no dia atual.
         */
        if (!savedDate.equals(getToday())) {
            clearRecords();
            return 0;
        }

        /*
         * Utiliza o valor persistido quando disponível.
         */
        if (preferences.contains(KEY_ACCUMULATED)) {
            return preferences.getInt(
                    KEY_ACCUMULATED,
                    0
            );
        }

        /*
         * Caso o valor acumulado não esteja armazenado,
         * calcula novamente a partir dos registros.
         */
        int accumulated = 0;

        List<WaterRecord> records = getRecords();

        for (WaterRecord record : records) {
            accumulated += record.getVolume();
        }

        /*
         * Salva o valor calculado para as próximas consultas.
         */
        preferences.edit()
                .putInt(
                        KEY_ACCUMULATED,
                        accumulated
                )
                .apply();

        return accumulated;
    }

    /*
     * Remove os registros, o consumo acumulado e a data
     * armazenada.
     *
     * A meta diária não é removida, pois ela deve permanecer
     * definida mesmo quando os registros do dia anterior
     * são descartados.
     */
    public void clearRecords() {
        preferences.edit()
                .remove(KEY_RECORDS)
                .remove(KEY_ACCUMULATED)
                .remove(KEY_DATE)
                .apply();
    }

    /*
     * Retorna a data atual no formato yyyy-MM-dd.
     *
     * Essa data é utilizada para diferenciar os registros
     * do dia atual dos registros de dias anteriores.
     */
    private String getToday() {

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date()
        );
    }
}
