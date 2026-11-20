package br.botaovermelho

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

/** Botão sempre quadrado (o fundo oval vira um círculo), do maior tamanho que couber. */
class SquareButton(context: Context, attrs: AttributeSet?) : Button(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val side = minOf(measuredWidth, measuredHeight)
        setMeasuredDimension(side, side)
    }
}
