package com.matchminds.kids.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.matchminds.core.Face
import kotlin.math.*

val Ink = Color(0xFF233D42)
val Teal = Color(0xFF237D70)
val Cream = Color(0xFFFFF9EE)
val Orange = Color(0xFFE98845)
val Gold = Color(0xFFF3BC4E)
val Pale = Color(0xFFEAF4ED)

private fun DrawScope.oval(c: Color, x: Float, y: Float, w: Float, h: Float) = drawOval(c, Offset(x,y), Size(w,h))
private fun DrawScope.line(c: Color, x: Float, y: Float, xx: Float, yy: Float, width: Float = 4f) = drawLine(c,Offset(x,y),Offset(xx,yy),width,StrokeCap.Round)
private fun DrawScope.poly(c: Color, vararg points: Float) { drawPath(Path().apply { moveTo(points[0],points[1]); for (i in 2 until points.size step 2) lineTo(points[i],points[i+1]); close() },c) }
private fun DrawScope.star(c: Color, cx: Float, cy: Float, r: Float) {
    drawPath(Path().apply { repeat(10) { i -> val a = (i * PI / 5 - PI / 2); val rr = if(i % 2 == 0) r else r*.47f; val x=cx+cos(a).toFloat()*rr; val y=cy+sin(a).toFloat()*rr; if(i==0) moveTo(x,y) else lineTo(x,y) }; close() },c)
}
private fun DrawScope.eyes(y: Float = 48f, wide: Float = 15f) {
    oval(Ink,50-wide-3,y,6f,9f); oval(Ink,50+wide-3,y,6f,9f)
    oval(Color.White,50-wide-1,y+1,2f,2f); oval(Color.White,50+wide-1,y+1,2f,2f)
}
private fun DrawScope.smile(y: Float = 69f) { drawArc(Ink,0f,180f,false,Offset(44f,y-4),Size(12f,7f),style=Stroke(2.5f,cap=StrokeCap.Round)) }

fun DrawScope.drawArt(key: String) {
    val white=Color(0xFFFFF4D9); val pink=Color(0xFFF0B2A3); val grey=Color(0xFF99BAC3)
    when(key.substringAfter(':')) {
        "fox" -> {
            poly(Orange,18f,37f,17f,8f,42f,27f); poly(Orange,58f,27f,83f,8f,82f,39f)
            poly(pink,23f,31f,23f,17f,35f,28f); poly(pink,65f,28f,77f,17f,77f,31f)
            oval(Orange,16f,24f,68f,62f)
            poly(white,19f,52f,50f,65f,81f,52f,72f,76f,50f,89f,28f,76f)
            eyes(43f); oval(Ink,44f,63f,12f,8f); smile(77f)
        }
        "lion" -> { oval(Color(0xFFD49548),8f,14f,84f,78f); oval(Gold,21f,25f,58f,58f); oval(Gold,15f,22f,16f,16f); oval(Gold,69f,22f,16f,16f); eyes(); oval(white,35f,61f,30f,18f); poly(Ink,44f,62f,56f,62f,50f,69f); smile(73f) }
        "elephant" -> { oval(grey,4f,29f,36f,53f); oval(grey,60f,29f,36f,53f); oval(pink,10f,39f,23f,32f); oval(pink,67f,39f,23f,32f); oval(grey,23f,19f,54f,62f); eyes(43f); drawPath(Path().apply {moveTo(43f,62f);lineTo(43f,85f);quadraticBezierTo(55f,101f,68f,84f);lineTo(61f,77f);quadraticBezierTo(55f,86f,56f,62f);close()},grey) }
        "rabbit" -> { oval(white,24f,1f,20f,56f); oval(white,56f,1f,20f,56f); oval(pink,30f,9f,8f,32f); oval(pink,62f,9f,8f,32f); oval(white,18f,32f,64f,57f); eyes(50f); poly(pink,45f,67f,55f,67f,50f,73f); smile(79f) }
        "panda" -> { oval(Ink,15f,17f,25f,25f); oval(Ink,60f,17f,25f,25f); oval(white,16f,26f,68f,62f); oval(Ink,27f,42f,18f,23f); oval(Ink,55f,42f,18f,23f); oval(Color.White,33f,49f,5f,7f); oval(Color.White,62f,49f,5f,7f); oval(Ink,45f,67f,10f,6f); smile(79f) }
        "penguin" -> { oval(Ink,18f,8f,64f,85f); oval(white,26f,33f,48f,53f); eyes(43f); poly(Gold,43f,57f,57f,57f,50f,67f); oval(Gold,18f,85f,29f,10f); oval(Gold,53f,85f,29f,10f) }
        "monkey" -> { oval(Color(0xFFA67B58),5f,34f,30f,34f); oval(Color(0xFFA67B58),65f,34f,30f,34f); oval(Color(0xFFA67B58),20f,15f,60f,72f); oval(Color(0xFFE9CB99),26f,35f,48f,47f); eyes(); smile(70f) }
        "giraffe" -> { line(Gold,50f,70f,50f,97f,24f); line(Color(0xFFAE7642),32f,26f,29f,10f,5f); line(Color(0xFFAE7642),68f,26f,71f,10f,5f); oval(Gold,17f,22f,66f,61f); oval(white,30f,60f,40f,22f); eyes(42f); oval(Color(0xFFAE7642),23f,30f,9f,8f); oval(Color(0xFFAE7642),64f,30f,10f,7f); smile(73f) }
        "zebra" -> { poly(white,23f,40f,22f,7f,39f,31f); poly(white,61f,31f,78f,7f,77f,40f); oval(white,20f,25f,60f,61f); repeat(3) { i -> line(Ink,35f+i*15f,27f,39f+i*11f,39f,5f) }; eyes(46f); oval(grey,28f,62f,44f,24f); smile(76f) }
        "cat" -> { poly(Gold,18f,40f,19f,12f,43f,30f); poly(Gold,57f,30f,81f,12f,82f,40f); oval(Gold,17f,27f,66f,59f); eyes(); poly(pink,45f,64f,55f,64f,50f,71f); line(Ink,22f,65f,37f,68f,2f);line(Ink,63f,68f,78f,65f,2f); smile(78f) }
        "dog" -> { oval(Color(0xFFA67B58),12f,21f,24f,58f); oval(Color(0xFFA67B58),64f,21f,24f,58f); oval(Color(0xFFE5C795),26f,22f,48f,64f); eyes(); oval(white,34f,60f,32f,20f); oval(Ink,43f,61f,14f,9f); smile(75f) }
        "dolphin" -> { oval(Color(0xFF78B9CF),15f,32f,66f,42f); poly(Color(0xFF78B9CF),38f,35f,52f,13f,58f,38f); poly(Color(0xFF78B9CF),76f,54f,97f,37f,92f,60f,97f,76f); oval(Color(0xFF78B9CF),5f,53f,29f,13f); oval(Ink,30f,46f,6f,8f); line(Ink,12f,63f,30f,65f,2f) }
        "circle","square","triangle","rectangle","star","heart","oval","diamond" -> {
            val c = when(key.substringBefore(':')) { "blue" -> Color(0xFF4F98C5); "coral" -> Color(0xFFE78A76); "green" -> Teal; "purple" -> Color(0xFF9986C5); else -> Gold }
            when(key.substringAfter(':')) {
                "circle" -> drawCircle(c,33f,Offset(50f,50f))
                "square" -> drawRoundRect(c,Offset(19f,19f),Size(62f,62f),CornerRadius(8f))
                "rectangle" -> drawRoundRect(c,Offset(8f,28f),Size(84f,44f),CornerRadius(7f))
                "triangle" -> poly(c,50f,10f,91f,83f,9f,83f)
                "diamond" -> poly(c,50f,8f,86f,50f,50f,92f,14f,50f)
                "oval" -> oval(c,12f,25f,76f,50f)
                "star" -> star(c,50f,50f,41f)
                "heart" -> drawPath(Path().apply { moveTo(50f,84f); cubicTo(-18f,44f,17f,-5f,50f,30f); cubicTo(83f,-5f,118f,44f,50f,84f) },c)
            }
        }
        "book" -> { poly(Teal,10f,23f,47f,28f,47f,85f,10f,77f); poly(Color(0xFF58AA98),53f,28f,90f,23f,90f,77f,53f,85f); repeat(3){line(white,19f,40f+it*13f,37f,43f+it*13f,3f);line(white,63f,43f+it*13f,81f,40f+it*13f,3f)} }
        "cup" -> { drawArc(Orange,-90f,180f,false,Offset(56f,29f),Size(34f,39f),style=Stroke(8f)); drawRoundRect(Teal,Offset(19f,23f),Size(48f,58f),CornerRadius(12f)); line(white,29f,31f,57f,31f,4f) }
        "spoon" -> { line(grey,50f,46f,50f,91f,10f); oval(grey,32f,7f,36f,48f); oval(white,39f,14f,17f,24f) }
        "chair" -> { line(Orange,25f,48f,25f,89f,8f); line(Orange,75f,48f,75f,89f,8f); drawRoundRect(Gold,Offset(20f,12f),Size(60f,42f),CornerRadius(9f)); line(Orange,20f,58f,80f,58f,10f) }
        "shoes" -> { drawRoundRect(Teal,Offset(6f,24f),Size(38f,58f),CornerRadius(13f)); drawRoundRect(Orange,Offset(55f,24f),Size(38f,58f),CornerRadius(13f)); repeat(3) { line(white,17f,35f+it*10f,33f,35f+it*10f,3f);line(white,66f,35f+it*10f,82f,35f+it*10f,3f) } }
        "backpack" -> { drawArc(Orange,180f,180f,false,Offset(32f,8f),Size(36f,33f),style=Stroke(7f)); drawRoundRect(Teal,Offset(17f,27f),Size(66f,65f),CornerRadius(16f)); drawRoundRect(Gold,Offset(28f,56f),Size(44f,25f),CornerRadius(5f)); line(white,34f,63f,66f,63f,3f) }
        "bicycle" -> { drawCircle(Teal,20f,Offset(25f,67f),style=Stroke(5f));drawCircle(Teal,20f,Offset(78f,67f),style=Stroke(5f));line(Orange,25f,67f,41f,35f);line(Orange,41f,35f,55f,67f);line(Orange,55f,67f,25f,67f);line(Orange,55f,67f,70f,35f);line(Orange,70f,35f,78f,67f);line(Ink,32f,31f,46f,31f);line(Ink,67f,26f,76f,26f) }
        "ball" -> { drawCircle(Gold,37f,Offset(50f,50f)); drawArc(Orange,0f,360f,false,Offset(32f,13f),Size(36f,74f),style=Stroke(5f));line(Teal,14f,50f,86f,50f,5f) }
        "toothbrush" -> { line(Teal,30f,85f,65f,25f,12f); line(Orange,64f,26f,77f,8f,15f);repeat(4){ line(white,57f+it*4,25f-it*6,72f+it*4,34f-it*6,3f) } }
        "clock" -> { drawCircle(Teal,40f,Offset(50f,50f));drawCircle(white,33f,Offset(50f,50f));line(Ink,50f,50f,50f,27f);line(Ink,50f,50f,66f,58f); repeat(4){ i -> val a=i*PI/2;drawCircle(Orange,3f,Offset(50f+cos(a).toFloat()*26,50f+sin(a).toFloat()*26)) } }
        "pattern" -> { star(Orange,24f,32f,20f);drawCircle(Teal,15f,Offset(74f,32f));star(Orange,24f,76f,20f);drawCircle(Teal,15f,Offset(74f,76f)) }
        "number" -> { drawArc(Teal,-130f,260f,false,Offset(24f,12f),Size(46f,38f),style=Stroke(9f,cap=StrokeCap.Round));drawArc(Teal,-100f,260f,false,Offset(24f,48f),Size(46f,38f),style=Stroke(9f,cap=StrokeCap.Round)) }
        "letter" -> { line(Teal,20f,84f,50f,14f,9f);line(Teal,50f,14f,80f,84f,9f);line(Orange,33f,58f,67f,58f,8f) }
        else -> { star(Gold,50f,50f,39f) }
    }
}

@Composable fun Art(key: String, modifier: Modifier = Modifier) {
    Canvas(modifier) { val side = minOf(size.width,size.height); translate((size.width-side)/2,(size.height-side)/2) { scale(side/100f,side/100f,Offset.Zero) { drawArt(key) } } }
}
@Composable fun FaceArt(face: Face, modifier: Modifier = Modifier) {
    when(face.art) {
        "digit", "letter" -> Box(modifier, contentAlignment = androidx.compose.ui.Alignment.Center) { Text(face.label,fontSize=if(face.art=="letter") 42.sp else 38.sp,fontWeight=FontWeight.ExtraBold,color=Teal) }
        "quantity" -> Canvas(modifier) {
            val cols=if(face.quantity<=6) 3 else 5; val rows=(face.quantity+cols-1)/cols
            val unit=minOf(size.width/(cols+1),size.height/(rows+1))
            repeat(face.quantity){ i -> star(Gold,size.width/2+(i%cols-(cols-1)/2f)*unit,size.height/2+(i/cols-(rows-1)/2f)*unit,unit*.36f) }
        }
        else -> Art(face.art,modifier)
    }
}
@Composable fun Milo(modifier: Modifier = Modifier, pose: String = "welcome") {
    Canvas(modifier) {
        val side=minOf(size.width,size.height); translate((size.width-side)/2,(size.height-side)/2) { scale(side/100f,side/100f,Offset.Zero) {
            oval(Color(0xFFE5E7D3),13f,89f,74f,8f)
            // Curled tail and cream tip, body and feet share the mascot palette.
            oval(Orange,55f,62f,37f,28f); poly(Color(0xFFFFF4D9),81f,62f,92f,68f,91f,83f,79f,80f)
            oval(Orange,31f,49f,38f,42f); oval(Color(0xFFFFF4D9),39f,59f,22f,27f)
            oval(Ink,29f,86f,20f,8f); oval(Ink,53f,86f,20f,8f)
            val raised=pose in listOf("welcome","celebrating","completion")
            line(Orange,35f,62f,if(raised)17f else 22f,if(raised)45f else 77f,10f)
            line(Orange,65f,62f,if(pose=="explaining")88f else 78f,if(raised)44f else 73f,10f)
            translate(9f,-2f) { scale(.82f,.72f,Offset.Zero){drawArt("fox")} }
            if(pose=="thinking") { oval(Gold,81f,19f,5f,5f); oval(Gold,87f,10f,8f,8f) }
            if(pose=="celebrating" || pose=="completion") { star(Gold,12f,17f,8f);star(Gold,90f,29f,7f) }
        } }
    }
}
