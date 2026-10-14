package com.example.watercontrol;

/*
 * Representa um registro individual de consumo de água.
 *
 * Cada registro armazena o volume consumido e o momento
 * em que o consumo foi registrado.
 */
public class WaterRecord {

    /* Volume de água consumido, em mililitros. */
    private final int volume;

    /* Momento do registro, armazenado como timestamp. */
    private final long timestamp;

    /*
     * Cria um registro de consumo com o volume informado
     * e o horário em que ele foi realizado.
     */
    public WaterRecord(int volume, long timestamp) {
        this.volume = volume;
        this.timestamp = timestamp;
    }

    /* Retorna o volume de água consumido. */
    public int getVolume() {
        return volume;
    }

    /* Retorna o momento em que o consumo foi registrado. */
    public long getTimestamp() {
        return timestamp;
    }
}
