package mindustry.client.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.style.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class MonospacedBar extends Bar{

    public MonospacedBar(String name, Color color, Floatp fraction){
        super(name, color, fraction);
    }

    public MonospacedBar(Prov<String> name, Prov<Color> color, Floatp fraction){
        super(name, color, fraction);
    }

    @Override
    public void draw(){
        if(fraction == null) return;

        float computed = Mathf.clamp(fraction.get());
        if(!Mathf.equal(lastValue, computed)){
            blink = 1f;
            lastValue = computed;
        }

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        Draw.colorl(0.1f);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);

        Drawable top = Tex.barTop;
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("bar-top").getWidth()){
            top.draw(x, y, topWidth, height);
        }else{
            if(ScissorStack.pushScissors(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("bar-top").getWidth(), height);
                ScissorStack.popScissors();
            }
        }

        Draw.color();

        BitmapFont font = Fonts.mono;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, name);

        font.setColor(Color.white);
        font.draw(name, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);

        Pools.free(lay);
    }
}
