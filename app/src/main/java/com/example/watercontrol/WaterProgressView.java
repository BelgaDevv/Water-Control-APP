package com.example.watercontrol;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class WaterProgressView extends View {

    /*
     * Objetos utilizados para desenhar o gráfico e os textos.
     */
    private final Paint progressPaint;
    private final Paint backgroundPaint;
    private final Paint textPaint;
    private final Paint titlePaint;

    /*
     * Configurações recebidas pelos atributos personalizados
     * definidos em attrs.xml.
     */
    private int progressColor;
    private int textColor;
    private int maxValue;

    /*
     * currentValue representa o consumo real atual.
     * animatedProgress representa o valor utilizado durante
     * a animação do gráfico.
     */
    private int currentValue = 0;
    private float animatedProgress = 0;

    /*
     * Define se o valor central será exibido como porcentagem
     * ou como consumo atual em ml.
     */
    private boolean percentageMode = true;

    private ValueAnimator progressAnimator;

    public WaterProgressView(
            Context context,
            AttributeSet attrs
    ) {
        super(context, attrs);

        /*
         * Obtém os atributos personalizados definidos no XML.
         */
        TypedArray attributes =
                context.obtainStyledAttributes(
                        attrs,
                        R.styleable.WaterProgressView
                );

        progressColor = attributes.getColor(
                R.styleable.WaterProgressView_progressColor,
                0xFF2196F3
        );

        textColor = attributes.getColor(
                R.styleable.WaterProgressView_textColor,
                0xFF000000
        );

        maxValue = attributes.getInt(
                R.styleable.WaterProgressView_maxValue,
                2000
        );

        /*
         * Libera os recursos utilizados pelo TypedArray.
         */
        attributes.recycle();

        /*
         * Configuração do indicador de progresso.
         */
        progressPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        progressPaint.setStyle(
                Paint.Style.STROKE
        );

        progressPaint.setStrokeWidth(25f);

        progressPaint.setStrokeCap(
                Paint.Cap.ROUND
        );

        /*
         * Configuração do círculo de fundo.
         */
        backgroundPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        backgroundPaint.setColor(
                0xFFE0E0E0
        );

        backgroundPaint.setStyle(
                Paint.Style.STROKE
        );

        backgroundPaint.setStrokeWidth(25f);

        /*
         * Configuração do texto central do gráfico.
         */
        textPaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint.setColor(textColor);

        textPaint.setTextSize(50f);

        textPaint.setTextAlign(
                Paint.Align.CENTER
        );

        /*
         * Configuração do texto do título.
         */
        titlePaint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        titlePaint.setColor(textColor);

        titlePaint.setTextSize(30f);

        titlePaint.setTextAlign(
                Paint.Align.CENTER
        );

        /*
         * Permite que a View receba eventos de clique.
         */
        setClickable(true);
    }

    /*
     * Desenha o gráfico e os textos da View.
     */
    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        /*
         * Calcula o centro da View para posicionar o gráfico.
         */
        float centerX =
                getWidth() / 2f;

        float centerY =
                getHeight() / 2f;

        /*
         * Calcula o raio do círculo de acordo com o tamanho
         * disponível na View.
         */
        float radius =
                Math.min(
                        getWidth(),
                        getHeight()
                ) / 2f - 40f;

        RectF circle = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        /*
         * Calcula a porcentagem do consumo em relação à meta.
         *
         * O valor não é limitado a 100%, permitindo que o texto
         * mostre valores como 120%, 150%, etc.
         */
        float percentage = 0f;

        if (maxValue > 0) {

            percentage =
                    (animatedProgress * 100f)
                            / maxValue;
        }

        /*
         * Verifica se o consumo atual ultrapassou a meta.
         */
        boolean goalExceeded =
                currentValue > maxValue;

        /*
         * Quando a meta é ultrapassada, o indicador muda
         * visualmente para vermelho.
         */
        if (goalExceeded) {

            progressPaint.setColor(
                    0xFFF44336
            );

        } else {

            progressPaint.setColor(
                    progressColor
            );
        }

        /*
         * Desenha o círculo de fundo, representando
         * o progresso que ainda pode ser realizado.
         */
        canvas.drawArc(
                circle,
                -90,
                360,
                false,
                backgroundPaint
        );

        /*
         * O desenho visual do arco é limitado a 100%.
         *
         * Isso evita que o arco dê uma volta adicional quando
         * o consumo ultrapassa a meta. O valor real continua
         * armazenado normalmente e a porcentagem pode ultrapassar
         * 100%.
         */
        float visualPercentage =
                Math.min(
                        percentage,
                        100f
                );

        canvas.drawArc(
                circle,
                -90,
                visualPercentage * 3.6f,
                false,
                progressPaint
        );

        /*
         * Define o texto que será exibido no centro.
         */
        String centerText;

        if (percentageMode) {

            centerText =
                    String.format(
                            "%.0f%%",
                            percentage
                    );

        } else {

            centerText =
                    currentValue
                            + "/"
                            + maxValue
                            + " ml";
        }

        textPaint.setColor(textColor);

        canvas.drawText(
                centerText,
                centerX,
                centerY + 15,
                textPaint
        );

        /*
         * Desenha o título da View.
         */
        titlePaint.setColor(textColor);

        canvas.drawText(
                "Consumo diário de água",
                centerX,
                centerY + radius + 35,
                titlePaint
        );

        /*
         * Exibe um alerta visual quando a meta é ultrapassada.
         */
        if (goalExceeded) {

            titlePaint.setColor(
                    0xFFF44336
            );

            canvas.drawText(
                    "Meta ultrapassada!",
                    centerX,
                    centerY + 55,
                    titlePaint
            );

            titlePaint.setColor(textColor);
        }
    }

    /*
     * Atualiza o consumo atual e inicia uma animação
     * entre o valor anterior e o novo valor.
     */
    public void setProgress(int value) {

        /*
         * Impede que valores negativos sejam utilizados.
         */
        if (value < 0) {
            value = 0;
        }

        int oldValue = currentValue;

        currentValue = value;

        /*
         * Cancela uma animação anterior caso exista.
         */
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        /*
         * Cria uma animação entre o valor anterior
         * e o novo valor.
         */
        progressAnimator =
                ValueAnimator.ofFloat(
                        oldValue,
                        value
                );

        progressAnimator.setDuration(500);

        /*
         * Atualiza o valor visual durante a animação
         * e solicita que a View seja redesenhada.
         */
        progressAnimator.addUpdateListener(
                animation -> {

                    animatedProgress =
                            (float) animation
                                    .getAnimatedValue();

                    invalidate();
                }
        );

        progressAnimator.start();
    }

    /*
     * Altera o valor máximo utilizado como referência
     * para o cálculo da porcentagem.
     */
    public void setMaxValue(int value) {

        if (value <= 0) {
            return;
        }

        maxValue = value;

        invalidate();
    }

    /*
     * Retorna o consumo atual.
     */
    public int getCurrentValue() {
        return currentValue;
    }

    /*
     * Retorna o valor máximo atual.
     */
    public int getMaxValue() {
        return maxValue;
    }

    /*
     * Alterna entre o modo de porcentagem e o modo
     * de consumo em ml.
     */
    @Override
    public boolean performClick() {

        super.performClick();

        percentageMode = !percentageMode;

        invalidate();

        return true;
    }

    /*
     * Detecta o toque do usuário na View.
     */
    @Override
    public boolean onTouchEvent(
            MotionEvent event
    ) {

        if (event.getAction()
                == MotionEvent.ACTION_UP) {

            performClick();

            return true;
        }

        return true;
    }
}
