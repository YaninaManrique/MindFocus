package com.diacode.mindfocus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.diacode.mindfocus.data.AppDatabase;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TimeFragment extends Fragment {
    private AppDatabase db;
    private TextView tvPorcentaje;
    private TextView tvTotal;
    private TextView tvCompletadas;
    private TextView tvProductividad;
    private DonutChartView donut;
    private BarChart barChart;
    private ProgressBar progressEstudio;
    private ProgressBar progressEjercicio;
    private ProgressBar progressHogar;
    private ProgressBar progressTrabajo;
    private ProgressBar progressCreativo;
    private TextView tvEstudioCantidad;
    private TextView tvEjercicioCantidad;
    private TextView tvHogarCantidad;
    private TextView tvTrabajoCantidad;
    private TextView tvCreativoCantidad;
    private TextView tvFrase;
    private TextView tvSemana;
    //frase aleatorias
    private final String[] frases = {
            "💡 Cada tarea completada te acerca a tus objetivos. ¡Sigue avanzando!",
            "🚀 El éxito es la suma de pequeños esfuerzos repetidos cada día.",
            "🎯 Concéntrate en el siguiente paso, no en toda la escalera.",
            "🌱 La constancia supera al talento cuando el talento no es constante.",
            "✨ Hoy es una nueva oportunidad para mejorar un poco más.",
            "🏆 No busques la perfección, busca el progreso.",
            "📚 Cada minuto de enfoque cuenta para alcanzar tus metas.",
            "🔥 La disciplina de hoy será el orgullo de mañana.",
            "💪 Nunca subestimes el poder de terminar una tarea.",
            "⭐ Avanza a tu ritmo, pero nunca te detengas."
    };
    public TimeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mitiempo, container, false);
        List<DonutChartView.DonutSlice> datos = new ArrayList<>();
        //dibuja los graficos
        db = AppDatabase.getInstance(requireContext());
        donut = view.findViewById(R.id.donut_chart);
        barChart = view.findViewById(R.id.barChart);
        tvTotal = view.findViewById(R.id.tvTotalTareas);
        tvPorcentaje = view.findViewById(R.id.tvPorcentaje);
        tvCompletadas = view.findViewById(R.id.tvCompletadas);
        tvProductividad = view.findViewById(R.id.tvProductividad);
        cargarReporte();
        donut.setSlices(datos);
        MaterialButton btn = view.findViewById(R.id.btnExportarPdf);
        btn.setOnClickListener(v -> exportPDF(view));
        //PROGRESO BARRAS
        progressEstudio = view.findViewById(R.id.progressEstudio);
        progressEjercicio = view.findViewById(R.id.progressEjercicio);
        progressHogar = view.findViewById(R.id.progressHogar);
        progressTrabajo = view.findViewById(R.id.progressTrabajo);
        progressCreativo = view.findViewById(R.id.progressCreativo);
        tvEstudioCantidad = view.findViewById(R.id.tvEstudioCantidad);
        tvEjercicioCantidad = view.findViewById(R.id.tvEjercicioCantidad);
        tvHogarCantidad = view.findViewById(R.id.tvHogarCantidad);
        tvTrabajoCantidad = view.findViewById(R.id.tvTrabajoCantidad);
        tvCreativoCantidad = view.findViewById(R.id.tvCreativoCantidad);
        //para las frases aleatorias
        tvFrase = view.findViewById(R.id.tvFrase);
        Random random = new Random();
        tvFrase.setText(frases[random.nextInt(frases.length)]);
        //para la fecha
        tvSemana = view.findViewById(R.id.tv_semana);
        //feccha actua;
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fecha = formato.format(new Date());
        tvSemana.setText("Fecha actual: " + fecha);
        return view;
    }
    private void exportPDF(View view){
        try {
            // ocultar boton para que no aparezca en el PDF
            MaterialButton btn = view.findViewById(R.id.btnExportarPdf);
            btn.setVisibility(View.GONE);
            // crear bitmap de toda la vista
            Bitmap bitmap = Bitmap.createBitmap(
                    view.getWidth(),
                    view.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            // mostrar nuevamente el botón
            btn.setVisibility(View.VISIBLE);
            // crear documento PDF
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo =
                    new PdfDocument.PageInfo.Builder(
                            bitmap.getWidth(),
                            bitmap.getHeight(),
                            1)
                            .create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas pdfCanvas = page.getCanvas();
            pdfCanvas.drawBitmap(bitmap, 0, 0, null);
            pdfDocument.finishPage(page);
            // nombre del archivo
            String fecha = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault())
                    .format(new Date());
            String nombreArchivo = "Reporte_MindFocus_" + fecha + ".pdf";
            File carpeta = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            File archivo = new File(carpeta, nombreArchivo);
            FileOutputStream fos = new FileOutputStream(archivo);
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();
            Toast.makeText(
                    requireContext(),
                    "PDF guardado en Descargas",
                    Toast.LENGTH_LONG
            ).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    requireContext(),
                    "Error al generar el PDF",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
    private void cargarReporte(){
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MindFocusPrefs", Context.MODE_PRIVATE);
        int usuarioId = prefs.getInt("usuarioId",-1);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->{
            int total = db.tareaDao().contarTodas(usuarioId);
            int completas = db.tareaDao().contarCompletadas(usuarioId);
            // cantidad de tareas por cada tipo
            int estudio = db.tareaDao().contarEstudio(usuarioId);
            int trabajo = db.tareaDao().contarTrabajo(usuarioId);
            int hogar = db.tareaDao().contarHogar(usuarioId);
            int ejercicio = db.tareaDao().contarEjercicio(usuarioId);
            int creativo = db.tareaDao().contarCreativo(usuarioId);

            // cantidad de tareas por prioridad
            int alta = db.tareaDao().contarAlta(usuarioId);
            int media = db.tareaDao().contarMedia(usuarioId);
            int baja = db.tareaDao().contarBaja(usuarioId);

            //PROGRESO
            int totalEstudio = db.tareaDao().totalEstudio(usuarioId);
            int estudioComp = db.tareaDao().estudioCompletadas(usuarioId);

            int totalEjercicio = db.tareaDao().totalEjercicio(usuarioId);
            int ejercicioComp = db.tareaDao().ejercicioCompletadas(usuarioId);

            int totalHogar = db.tareaDao().totalHogar(usuarioId);
            int hogarComp = db.tareaDao().hogarCompletadas(usuarioId);

            int totalTrabajo = db.tareaDao().totalTrabajo(usuarioId);
            int trabajoComp = db.tareaDao().trabajoCompletadas(usuarioId);

            int totalCreativo = db.tareaDao().totalCreativo(usuarioId);
            int creativoComp = db.tareaDao().creativoCompletadas(usuarioId);
            requireActivity().runOnUiThread(() ->{
                //-------------------------
                // Barra de progreso
                //-------------------------
                int porcentaje = 0;
                if(total>0)
                    porcentaje = completas*100/total;
                tvPorcentaje.setText("Progreso de hoy: "+porcentaje+"%");
                tvTotal.setText(String.valueOf(total));
                tvCompletadas.setText(String.valueOf("Llevas "+completas+" tareas completadas."));
                if(porcentaje>=80)
                    tvProductividad.setText("Excelente");
                else if(porcentaje>=50)
                    tvProductividad.setText("Buena");
                else
                    tvProductividad.setText("Debe mejorar");
                //-------------------------
                // DONUT
                //-------------------------
                List<DonutChartView.DonutSlice> datos = new ArrayList<>();
                // lista de secciones del grafico (se agrega color y cantidad)
                if(estudio>0)
                    datos.add(new DonutChartView.DonutSlice(Color.parseColor("#4CAF50"), estudio));
                if(trabajo>0)
                    datos.add(new DonutChartView.DonutSlice(Color.parseColor("#2196F3"), trabajo));
                if(hogar>0)
                    datos.add(new DonutChartView.DonutSlice(Color.parseColor("#FF9800"), hogar));
                if(ejercicio>0)
                    datos.add(new DonutChartView.DonutSlice(Color.parseColor("#9C27B0"), ejercicio));
                if(creativo>0)
                    datos.add(new DonutChartView.DonutSlice(Color.parseColor("#F44336"), creativo));
                // dibuja el grafico circular
                donut.setSlices(datos);
                //-------------------------
                // BARRAS
                //-------------------------
                // lista de barras
                ArrayList<BarEntry> entries = new ArrayList<>();
                // posicion X y altura de cada barra
                entries.add(new BarEntry(0, baja));
                entries.add(new BarEntry(1, media));
                entries.add(new BarEntry(2, alta));
                // conjunto de datos del grafico
                BarDataSet set = new BarDataSet(entries,"Prioridad");
                // se asigna colores
                set.setColors(
                        Color.parseColor("#4CAF50"),
                        Color.parseColor("#FFC107"),
                        Color.parseColor("#F44336")
                );
                //para mostrar a enteros
                set.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value);
                    }
                });
                // se carga la informacion al grafico
                BarData data = new BarData(set);
                barChart.setData(data);
                // cambiar el nombre de los ejes
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(
                        new String[]{"Baja","Media","Alta"}
                ));
                xAxis.setGranularity(1f);
                // coloca el eje en la parte inferior
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setDrawGridLines(false);
                YAxis yAxisLeft = barChart.getAxisLeft();
                yAxisLeft.setGranularity(1f);
                yAxisLeft.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return String.valueOf((int) value);
                    }
                });
                barChart.getDescription().setEnabled(false);
                barChart.getAxisRight().setEnabled(false);
                barChart.animateY(1200);
                barChart.invalidate();

                actualizarTipo(
                        progressEstudio,
                        tvEstudioCantidad,
                        estudioComp,
                        totalEstudio
                );

                actualizarTipo(
                        progressEjercicio,
                        tvEjercicioCantidad,
                        ejercicioComp,
                        totalEjercicio
                );

                actualizarTipo(
                        progressHogar,
                        tvHogarCantidad,
                        hogarComp,
                        totalHogar
                );

                actualizarTipo(
                        progressTrabajo,
                        tvTrabajoCantidad,
                        trabajoComp,
                        totalTrabajo
                );

                actualizarTipo(
                        progressCreativo,
                        tvCreativoCantidad,
                        creativoComp,
                        totalCreativo
                );

            });
        });
    }
    private void actualizarTipo(
            ProgressBar progressBar,
            TextView cantidad,
            int completadas,
            int total
    ){
        cantidad.setText(completadas + "/" + total);
        if(total == 0){
            progressBar.setProgress(0);
            return;
        }
        int porcentaje = completadas * 100 / total;
        progressBar.setProgress(porcentaje);
    }
    //recargar
    @Override
    public void onResume() {
        super.onResume();
        cargarReporte();
    }
}