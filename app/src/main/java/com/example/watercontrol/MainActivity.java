package com.example.watercontrol;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /*
     * View responsável por exibir visualmente o progresso
     * do consumo de água.
     */
    private WaterProgressView waterProgressView;

    /*
     * Campos utilizados para informar o consumo de água
     * e a meta diária.
     */
    private EditText editTextWaterAmount;
    private EditText editTextDailyGoal;

    /*
     * TextViews utilizados para apresentar o consumo acumulado
     * e os registros realizados durante o dia.
     */
    private TextView textViewAccumulated;
    private TextView textViewRecords;

    /*
     * Botões responsáveis por registrar um consumo
     * e alterar a meta diária.
     */
    private Button buttonRegisterWater;
    private Button buttonChangeGoal;

    /*
     * Responsável pela persistência dos dados do aplicativo.
     */
    private WaterStorage waterStorage;

    /*
     * Lista contendo os registros de consumo do dia atual.
     */
    private List<WaterRecord> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Carrega a interface definida no XML.
         */
        setContentView(R.layout.activity_main);

        /*
         * Localiza os componentes da interface.
         */
        initializeViews();

        /*
         * Inicializa o sistema responsável pelo armazenamento
         * persistente dos dados.
         */
        waterStorage = new WaterStorage(this);

        /*
         * Recupera os registros armazenados.
         *
         * O WaterStorage também verifica se os registros
         * pertencem ao dia atual.
         */
        records = waterStorage.getRecords();

        /*
         * Atualiza a interface com os dados armazenados.
         */
        updateInterface();

        /*
         * Define a ação executada ao registrar um consumo.
         */
        buttonRegisterWater.setOnClickListener(
                view -> registerWater()
        );

        /*
         * Define a ação executada ao alterar a meta.
         */
        buttonChangeGoal.setOnClickListener(
                view -> changeGoal()
        );
    }

    /*
     * Localiza os componentes da interface através
     * dos IDs definidos no activity_main.xml.
     */
    private void initializeViews() {

        waterProgressView = findViewById(
                R.id.waterProgressView
        );

        editTextWaterAmount = findViewById(
                R.id.editTextWaterAmount
        );

        editTextDailyGoal = findViewById(
                R.id.editTextDailyGoal
        );

        textViewAccumulated = findViewById(
                R.id.textViewAccumulated
        );

        textViewRecords = findViewById(
                R.id.textViewRecords
        );

        buttonRegisterWater = findViewById(
                R.id.buttonRegisterWater
        );

        buttonChangeGoal = findViewById(
                R.id.buttonChangeGoal
        );
    }

    /*
     * Registra um novo consumo de água.
     */
    private void registerWater() {

        String amountText =
                editTextWaterAmount
                        .getText()
                        .toString()
                        .trim();

        /*
         * Verifica se o usuário informou algum valor.
         */
        if (amountText.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.water_amount_required),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int amount;

        try {

            /*
             * Converte o valor informado para inteiro.
             */
            amount = Integer.parseInt(amountText);

        } catch (NumberFormatException e) {

            /*
             * Trata valores que não podem ser convertidos
             * para um número inteiro.
             */
            Toast.makeText(
                    this,
                    getString(R.string.invalid_water_amount),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Impede o registro de volumes iguais ou menores que zero.
         */
        if (amount <= 0) {

            Toast.makeText(
                    this,
                    getString(R.string.water_amount_positive),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Cria um registro contendo o volume informado
         * e o horário atual.
         */
        WaterRecord record = new WaterRecord(
                amount,
                System.currentTimeMillis()
        );

        /*
         * Adiciona o novo registro à lista do dia.
         */
        records.add(record);

        /*
         * Persiste a lista atualizada.
         */
        waterStorage.saveRecords(records);

        /*
         * Limpa o campo de entrada após o registro.
         */
        editTextWaterAmount.setText("");

        /*
         * Atualiza todos os elementos da interface.
         */
        updateInterface();
    }

    /*
     * Altera a meta diária de consumo.
     */
    private void changeGoal() {

        String goalText =
                editTextDailyGoal
                        .getText()
                        .toString()
                        .trim();

        /*
         * Verifica se o usuário informou uma nova meta.
         */
        if (goalText.isEmpty()) {

            Toast.makeText(
                    this,
                    getString(R.string.goal_required),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int goal;

        try {

            /*
             * Converte a meta informada para inteiro.
             */
            goal = Integer.parseInt(goalText);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    getString(R.string.invalid_goal),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * A meta precisa ser maior que zero.
         */
        if (goal <= 0) {

            Toast.makeText(
                    this,
                    getString(R.string.goal_positive),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Salva a nova meta.
         *
         * Os registros existentes não são alterados.
         */
        waterStorage.saveGoal(goal);

        /*
         * Limpa o campo após a alteração.
         */
        editTextDailyGoal.setText("");

        /*
         * Atualiza o gráfico e os demais elementos da interface.
         */
        updateInterface();
    }

    /*
     * Atualiza todos os elementos da interface com os
     * dados atualmente armazenados.
     */
    private void updateInterface() {

        /*
         * Recupera novamente os registros para garantir
         * que a interface esteja utilizando os dados atuais.
         */
        records = waterStorage.getRecords();

        /*
         * Recupera a meta diária.
         */
        int goal = waterStorage.getGoal();

        /*
         * Recupera o consumo acumulado.
         */
        int accumulated =
                waterStorage.getAccumulatedConsumption();

        /*
         * Atualiza a meta utilizada pelo gráfico.
         */
        waterProgressView.setMaxValue(goal);

        /*
         * Atualiza o consumo exibido pelo gráfico.
         *
         * A WaterProgressView também realiza a animação
         * entre o valor anterior e o novo valor.
         */
        waterProgressView.setProgress(accumulated);

        /*
         * Atualiza o texto com o consumo acumulado.
         */
        textViewAccumulated.setText(
                getString(
                        R.string.accumulated_format,
                        accumulated
                )
        );

        /*
         * Atualiza a lista de registros exibida na tela.
         */
        updateRecordsText();
    }

    /*
     * Monta o texto contendo todos os registros
     * realizados durante o dia.
     */
    private void updateRecordsText() {

        /*
         * Quando não existem registros, exibe uma mensagem
         * informando que nenhum consumo foi registrado.
         */
        if (records.isEmpty()) {

            textViewRecords.setText(
                    getString(R.string.empty_history)
            );

            return;
        }

        StringBuilder text = new StringBuilder();

        text.append(
                getString(R.string.records_today)
        );

        text.append("\n\n");

        /*
         * Formata o timestamp de cada registro para
         * apresentar somente hora e minuto.
         */
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                );

        /*
         * Percorre todos os registros do dia e adiciona
         * cada um à área de histórico.
         */
        for (WaterRecord record : records) {

            String time = formatter.format(
                    new java.util.Date(
                            record.getTimestamp()
                    )
            );

            text.append(
                    getString(
                            R.string.record_format,
                            time,
                            record.getVolume()
                    )
            );

            text.append("\n");
        }

        /*
         * Exibe o histórico completo na interface.
         */
        textViewRecords.setText(
                text.toString()
        );
    }
}