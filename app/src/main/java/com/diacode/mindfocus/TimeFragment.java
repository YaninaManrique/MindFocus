package com.diacode.mindfocus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private View segEstudioCompletado, segEstudioPendiente;
    private View segEjercicioCompletado, segEjercicioPendiente;
    private View segHogarCompletado, segHogarPendiente;
    private View segTrabajoCompletado, segTrabajoPendiente;
    private View segCreativoCompletado, segCreativoPendiente;
    private LinearLayout layoutDiasStats;
    private final String[] nombresDias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private List<MaterialButton> botonesDiasStats = new ArrayList<>();
    private List<Calendar> fechasSemanaStats = new ArrayList<>();
    private int diaSeleccionadoStats = 0;
    // variable para controlar la semana actual en visualización
    private Calendar semanaPivot = Calendar.getInstance();
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

        //para la fecha
        tvSemana = view.findViewById(R.id.tv_semana);
        donut.setSlices(datos);

        layoutDiasStats = view.findViewById(R.id.layout_dias_stats);

        ImageButton btnSemanaAnterior = view.findViewById(R.id.btnSemanaAnterior);
        ImageButton btnSemanaSiguiente = view.findViewById(R.id.btnSemanaSiguiente);
        btnSemanaAnterior.setOnClickListener(v -> {
            // restamos 7 días para ir a la semana pasada
            semanaPivot.add(Calendar.DAY_OF_MONTH, -7);
            generarChipsSemanaStats();
        });
        btnSemanaSiguiente.setOnClickListener(v -> {
            // sumamos 7 días para ir a la semana que viene
            semanaPivot.add(Calendar.DAY_OF_MONTH, 7);
            generarChipsSemanaStats();
        });
        generarChipsSemanaStats();

        MaterialButton btn = view.findViewById(R.id.btnExportarPdf);
        btn.setOnClickListener(v -> exportPDFV2(view));
        //PROGRESO BARRAS
        segEstudioCompletado = view.findViewById(R.id.segEstudioCompletado);
        segEstudioPendiente = view.findViewById(R.id.segEstudioPendiente);
        segEjercicioCompletado = view.findViewById(R.id.segEjercicioCompletado);
        segEjercicioPendiente = view.findViewById(R.id.segEjercicioPendiente);
        segHogarCompletado = view.findViewById(R.id.segHogarCompletado);
        segHogarPendiente = view.findViewById(R.id.segHogarPendiente);
        segTrabajoCompletado = view.findViewById(R.id.segTrabajoCompletado);
        segTrabajoPendiente = view.findViewById(R.id.segTrabajoPendiente);
        segCreativoCompletado = view.findViewById(R.id.segCreativoCompletado);
        segCreativoPendiente = view.findViewById(R.id.segCreativoPendiente);

        tvEstudioCantidad = view.findViewById(R.id.tvEstudioCantidad);
        tvEjercicioCantidad = view.findViewById(R.id.tvEjercicioCantidad);
        tvHogarCantidad = view.findViewById(R.id.tvHogarCantidad);
        tvTrabajoCantidad = view.findViewById(R.id.tvTrabajoCantidad);
        tvCreativoCantidad = view.findViewById(R.id.tvCreativoCantidad);
        //para las frases aleatorias
        tvFrase = view.findViewById(R.id.tvFrase);
        Random random = new Random();
        tvFrase.setText(frases[random.nextInt(frases.length)]);


        //feccha actua;
//        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        String fecha = formato.format(new Date());
//        tvSemana.setText("Fecha actual: " + fecha);
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
    private void exportPDFV2(View view) {
        if (fechasSemanaStats == null || fechasSemanaStats.isEmpty()) return;
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MindFocusPrefs", Context.MODE_PRIVATE);
        int usuarioId = prefs.getInt("usuarioId", -1);

        // 1. capturar los graficos nativos tal cual están renderizados en la pantalla actual
        String donutBase64 = obtenerViewComoBase64(donut);
        String barChartBase64 = obtenerViewComoBase64(barChart);

        // datos estéticos directos de las vistas actuales
        String totalTareasHoy = tvTotal.getText().toString();
        String progresoPorcentajeHoy = tvPorcentaje.getText().toString();
        String productividadEstadoHoy = tvProductividad.getText().toString();
        String semanaTexto = tvSemana.getText().toString();
        String sugerenciaTexto = tvFrase.getText().toString();

        // 2. procesar datos en segundo plano para el análisis real de toda la semana
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            // variables para encontrar el día más y menos productivo de la semana
            String diaMasProductivo = "Ninguno";
            String diaMenosProductivo = "Ninguno";
            int maxPorcentaje = -1;
            int minPorcentaje = 101;

            // variables para acumular tareas pendientes y totales por categoría en toda la semana
            int totalEstudioSemana = 0, completadasEstudioSemana = 0;
            int totalEjercicioSemana = 0, completadasEjercicioSemana = 0;
            int totalHogarSemana = 0, completadasHogarSemana = 0;
            int totalTrabajoSemana = 0, completadasTrabajoSemana = 0;
            int totalCreativoSemana = 0, completadasCreativoSemana = 0;

            // iterar por los 7 días de la semana cargada para el análisis real
            for (int i = 0; i < fechasSemanaStats.size(); i++) {
                Calendar diaCal = fechasSemanaStats.get(i);
                long inicioDia = diaCal.getTimeInMillis();
                long finDia = inicioDia + (24L * 60 * 60 * 1000) - 1;

                // datos generales del día
                int t = db.tareaDao().contarTodasPorFecha(usuarioId, inicioDia, finDia);
                int c = db.tareaDao().contarCompletadasPorFecha(usuarioId, inicioDia, finDia);

                // calcular porcentaje del día
                int porc = 0;
                if (t > 0) {
                    porc = (c * 100) / t;

                    // evaluar día más productivo
                    if (porc > maxPorcentaje) {
                        maxPorcentaje = porc;
                        diaMasProductivo = nombresDias[i] + " (" + porc + "%)";
                    }
                    // evaluar día menos productivo
                    if (porc < minPorcentaje) {
                        minPorcentaje = porc;
                        diaMenosProductivo = nombresDias[i] + " (" + porc + "%)";
                    }
                }

                // aacumular categorías de la semana para detectar dónde se falla más
                totalEstudioSemana += db.tareaDao().totalEstudioPorFecha(usuarioId, inicioDia, finDia);
                completadasEstudioSemana += db.tareaDao().estudioCompletadasPorFecha(usuarioId, inicioDia, finDia);

                totalEjercicioSemana += db.tareaDao().totalEjercicioPorFecha(usuarioId, inicioDia, finDia);
                completadasEjercicioSemana += db.tareaDao().ejercicioCompletadasPorFecha(usuarioId, inicioDia, finDia);

                totalHogarSemana += db.tareaDao().totalHogarPorFecha(usuarioId, inicioDia, finDia);
                completadasHogarSemana += db.tareaDao().hogarCompletadasPorFecha(usuarioId, inicioDia, finDia);

                totalTrabajoSemana += db.tareaDao().totalTrabajoPorFecha(usuarioId, inicioDia, finDia);
                completadasTrabajoSemana += db.tareaDao().trabajoCompletadasPorFecha(usuarioId, inicioDia, finDia);

                totalCreativoSemana += db.tareaDao().totalCreativoPorFecha(usuarioId, inicioDia, finDia);
                completadasCreativoSemana += db.tareaDao().creativoCompletadasPorFecha(usuarioId, inicioDia, finDia);
            }

            // si no hubo tareas registradas en la semana, rellenamos con valores por defecto coherentes
            if (maxPorcentaje == -1) {
                diaMasProductivo = "Sin registros esta semana";
                diaMenosProductivo = "Sin registros esta semana";
            }

            // calcular porcentajes de falla por categoría (Tareas Pendientes / Total Tareas)
            double fallasHogar = calcularPorcentajeFalla(totalHogarSemana, completadasHogarSemana);
            double fallasEjercicio = calcularPorcentajeFalla(totalEjercicioSemana, completadasEjercicioSemana);
            double fallasEstudio = calcularPorcentajeFalla(totalEstudioSemana, completadasEstudioSemana);
            double fallasTrabajo = calcularPorcentajeFalla(totalTrabajoSemana, completadasTrabajoSemana);
            double fallasCreativo = calcularPorcentajeFalla(totalCreativoSemana, completadasCreativoSemana);

            // determinar dinámicamente cuál es la categoría en la que más se está fallando
            String categoriaFalloPrincipal = "Ninguna";
            double maxFalla = 0;

            if (fallasHogar > maxFalla) { maxFalla = fallasHogar; categoriaFalloPrincipal = "Hogar (" + (int)fallasHogar + "% de tareas pendientes)"; }
            if (fallasEjercicio > maxFalla) { maxFalla = fallasEjercicio; categoriaFalloPrincipal = "Ejercicio (" + (int)fallasEjercicio + "% de tareas pendientes)"; }
            if (fallasEstudio > maxFalla) { maxFalla = fallasEstudio; categoriaFalloPrincipal = "Estudio (" + (int)fallasEstudio + "% de tareas pendientes)"; }
            if (fallasTrabajo > maxFalla) { maxFalla = fallasTrabajo; categoriaFalloPrincipal = "Trabajo (" + (int)fallasTrabajo + "% de tareas pendientes)"; }
            if (fallasCreativo > maxFalla) { maxFalla = fallasCreativo; categoriaFalloPrincipal = "Creativo (" + (int)fallasCreativo + "% de tareas pendientes)"; }

            if (maxFalla == 0) {
                categoriaFalloPrincipal = "Ninguna (¡Excelente rendimiento general!)";
            }

            // calcular porcentajes individuales de completados vs pendientes para las barras de la pág 2
            int estudioCompPct = calcularPorcentajeCompletado(totalEstudioSemana, completadasEstudioSemana);
            int estudioPendPct = 100 - estudioCompPct;

            int ejercicioCompPct = calcularPorcentajeCompletado(totalEjercicioSemana, completadasEjercicioSemana);
            int ejercicioPendPct = 100 - ejercicioCompPct;

            int hogarCompPct = calcularPorcentajeCompletado(totalHogarSemana, completadasHogarSemana);
            int hogarPendPct = 100 - hogarCompPct;

            int trabajoCompPct = calcularPorcentajeCompletado(totalTrabajoSemana, completadasTrabajoSemana);
            int trabajoPendPct = 100 - trabajoCompPct;

            int creativoCompPct = calcularPorcentajeCompletado(totalCreativoSemana, completadasCreativoSemana);
            int creativoPendPct = 100 - creativoCompPct;

            // variables finales para inyectar en el hilo principal de UI (WebView)
            final String finalDiaMas = diaMasProductivo;
            final String finalDiaMenos = diaMenosProductivo;
            final String finalCategoriasFallo = categoriaFalloPrincipal;

            int finalTotalEjercicioSemana = totalEjercicioSemana;
            int finalTotalEstudioSemana = totalEstudioSemana;
            int finalTotalHogarSemana = totalHogarSemana;
            int finalTotalTrabajoSemana = totalTrabajoSemana;
            int finalTotalCreativoSemana = totalCreativoSemana;
            requireActivity().runOnUiThread(() -> {
                // 3. construir el HTML inyectando dinámicamente todas las métricas reales calculadas
                String htmlContent = "<html>" +
                        "<head>" +
                        "<style>" +
                        "  @page { size: A4; margin: 15mm; }" +
                        "  body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #2D3748; line-height: 1.5; }" +
                        "  .header { border-bottom: 2px solid #E2E8F0; padding-bottom: 10px; margin-bottom: 20px; }" +
                        "  .title { font-size: 22px; color: #1A365D; font-weight: bold; margin: 0; }" +
                        "  .cards-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }" +
                        "  .card { border: 1px solid #E2E8F0; padding: 15px; border-radius: 8px; text-align: center; background: #fff; width: 33%; }" +
                        "  .card-value { font-size: 20px; font-weight: bold; color: #2B6CB0; }" +
                        "  .section-title { font-size: 14px; color: #1A365D; border-left: 4px solid #3182CE; padding-left: 8px; margin-top: 25px; margin-bottom: 15px; text-transform: uppercase; }" +
                        "  .insight-box { background-color: #EBF8FF; border-left: 4px solid #3182CE; padding: 15px; border-radius: 4px; margin-bottom: 20px; }" +
                        "  .page-break { page-break-before: always; }" +

                        "  .charts-flex { display: table; width: 100%; margin-bottom: 20px; }" +
                        "  .chart-column { display: table-cell; width: 50%; text-align: center; vertical-align: middle; padding: 10px; }" +
                        "  .chart-img { max-width: 100%; height: auto; border: 1px solid #EDF2F7; border-radius: 6px; padding: 5px; }" +

                        "  .bar-container { background: #E2E8F0; height: 12px; border-radius: 6px; overflow: hidden; width: 100%; margin-bottom: 12px; }" +
                        "  .bar-fill { background: #48BB78; height: 100%; float: left; }" +
                        "  .bar-pending { background: #E53935; height: 100%; float: left; }" +
                        "  .task-label { font-size: 13px; font-weight: bold; margin-bottom: 4px; display: flex; justify-content: space-between; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +

                        // ================= PÁGINA 1 =================
                        "  <div class='header'>" +
                        "    <div class='title'>MindFocus - Reporte Semanal de Productividad</div>" +
                        "    <div style='color:#718096; font-size:13px;'>" + semanaTexto + "</div>" +
                        "  </div>" +

                        "  <table class='cards-table'><tr>" +
                        "    <td class='card'><div style='color:#718096; font-size:11px;'>TAREAS EN EL DÍA</div><div class='card-value'>" + totalTareasHoy + "</div></td>" +
                        "    <td class='card'><div style='color:#718096; font-size:11px;'>PRODUCTIVIDAD</div><div class='card-value'>" + productividadEstadoHoy + "</div></td>" +
                        "    <td class='card'><div style='color:#718096; font-size:11px;'>PROGRESO</div><div class='card-value'>" + progresoPorcentajeHoy + "</div></td>" +
                        "  </tr></table>" +

                        "  <div class='section-title'>Interpretación y Análisis del Rendimiento (Semanal)</div>" +
                        "  <div class='insight-box'>" +
                        "    <ul style='margin: 0; padding-left: 20px; font-size: 13px;'>" +
                        "      <li><strong>¿Cuál fue el día más productivo?</strong> El día más eficiente registrado fue el <span style='color:#2F855A; font-weight:bold;'>" + finalDiaMas + "</span>.</li>" +
                        "      <li><strong>¿Cuál fue el día menos productivo?</strong> El día con menor porcentaje de tareas completadas fue el <span style='color:#C53030; font-weight:bold;'>" + finalDiaMenos + "</span>.</li>" +
                        "      <li><strong>¿En qué categorías se está fallando más?</strong> El área con mayor rezago es <span style='font-weight:bold; color:#C53030;'>" + finalCategoriasFallo + "</span>.</li>" +
                        "    </ul>" +
                        "  </div>" +

                        "  <div class='section-title'>Visualización de Logros Diario y Prioridades</div>" +
                        "  <div class='charts-flex'>" +
                        "    <div class='chart-column'>" +
                        "       <div style='font-size:11px; color:#718096; margin-bottom:5px;'>Distribución por Tipo (Donut)</div>" +
                        "       <img class='chart-img' src='" + donutBase64 + "' style='height: 120px; width: auto;' />" +
                        "    </div>" +
                        "    <div class='chart-column'>" +
                        "       <div style='font-size:11px; color:#718096; margin-bottom:5px;'>Tareas por Prioridad</div>" +
                        "       <img class='chart-img' src='" + barChartBase64 + "' style='height: 140px; width: auto;' />" +
                        "    </div>" +
                        "  </div>" +

                        "  <div style='text-align: center; color: #A0AEC0; font-size: 10px; margin-top: 40px;'>Página 1 de 2</div>" +

                        // ================= PÁGINA 2 =================
                        "  <div class='page-break'></div>" +

                        "  <div class='header'>" +
                        "    <div class='title'>MindFocus - Detalle de Categorías</div>" +
                        "    <div style='color:#718096; font-size:13px;'>Rendimiento y tiempos invertidos por área</div>" +
                        "  </div>" +

                        "  <div class='section-title'>Análisis de Tareas por Categorías (Semanal)</div>" +

                        // Estudio
                        "  <div class='task-label'><span>📚 Estudio (" + estudioCompPct + "% completado)</span> <span>Total: " + finalTotalEstudioSemana + " tareas</span></div>" +
                        "  <div class='bar-container'>" +
                        "    <div class='bar-fill' style='width: " + estudioCompPct + "%;'></div>" +
                        "    <div class='bar-pending' style='width: " + estudioPendPct + "%;'></div>" +
                        "  </div>" +

                        // Ejercicio
                        "  <div class='task-label'><span>🏃 Ejercicio (" + ejercicioCompPct + "% completado)</span> <span>Total: " + finalTotalEjercicioSemana + " tareas</span></div>" +
                        "  <div class='bar-container'>" +
                        "    <div class='bar-fill' style='width: " + ejercicioCompPct + "%;'></div>" +
                        "    <div class='bar-pending' style='width: " + ejercicioPendPct + "%;'></div>" +
                        "  </div>" +

                        // Hogar
                        "  <div class='task-label'><span>🏠 Hogar (" + hogarCompPct + "% completado)</span> <span>Total: " + finalTotalHogarSemana + " tareas</span></div>" +
                        "  <div class='bar-container'>" +
                        "    <div class='bar-fill' style='width: " + hogarCompPct + "%;'></div>" +
                        "    <div class='bar-pending' style='width: " + hogarPendPct + "%;'></div>" +
                        "  </div>" +

                        // Trabajo
                        "  <div class='task-label'><span>💼 Trabajo (" + trabajoCompPct + "% completado)</span> <span>Total: " + finalTotalTrabajoSemana + " tareas</span></div>" +
                        "  <div class='bar-container'>" +
                        "    <div class='bar-fill' style='width: " + trabajoCompPct + "%;'></div>" +
                        "    <div class='bar-pending' style='width: " + trabajoPendPct + "%;'></div>" +
                        "  </div>" +

                        // Creativo
                        "  <div class='task-label'><span>🎨 Creativo (" + creativoCompPct + "% completado)</span> <span>Total: " + finalTotalCreativoSemana + " tareas</span></div>" +
                        "  <div class='bar-container'>" +
                        "    <div class='bar-fill' style='width: " + creativoCompPct + "%;'></div>" +
                        "    <div class='bar-pending' style='width: " + creativoPendPct + "%;'></div>" +
                        "  </div>" +

                        "  <div style='background-color: #FAF5FF; border-left: 4px solid #9F7AEA; padding: 15px; border-radius: 4px; margin-top: 30px;'>" +
                        "    <div style='color:#6B46C1; font-weight:bold; margin-bottom: 5px;'>💡 Sugerencias del Sistema</div>" +
                        "    <p style='margin:0; font-size:12px; color: #4A5568;'>" + sugerenciaTexto + "</p>" +
                        "  </div>" +

                        // ================= SECCIÓN INFORMATIVA =================
                        "  <div class='section-title' style='margin-top: 30px;'>Guía de Optimización del Tiempo y Enfoque</div>" +
                        "  <table style='width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 11px; color: #4A5568;'>" +
                        "    <tr>" +
                        "      <td style='width: 31%; border: 1px solid #E2E8F0; padding: 10px; vertical-align: top; border-radius: 6px 0 0 6px; background: #FFF;'>" +
                        "        <div style='font-weight: bold; color: #2B6CB0; margin-bottom: 5px;'>🎯 Regla del 80/20</div>" +
                        "        El 80% de tus resultados provienen del 20% de tus esfuerzos. Identifica las tareas de alto impacto (usualmente en prioridad Alta) y dales prioridad absoluta al iniciar tu jornada académica o laboral." +
                        "      </td>" +
                        "      <td style='width: 2%;'></td>" + // Espaciador
                        "      <td style='width: 31%; border: 1px solid #E2E8F0; padding: 10px; vertical-align: top; background: #FFF;'>" +
                        "        <div style='font-weight: bold; color: #2B6CB0; margin-bottom: 5px;'>🍅 Técnica Pomodoro</div>" +
                        "        Si te cuesta iniciar una categoría como 'Estudio', trabaja en bloques de 25 minutos de enfoque absoluto seguidos de 5 minutos de descanso. Al completar 4 bloques, toma un descanso largo de 15 a 30 minutos." +
                        "      </td>" +
                        "      <td style='width: 2%;'></td>" + // Espaciador
                        "      <td style='width: 31%; border: 1px solid #E2E8F0; padding: 10px; vertical-align: top; border-radius: 0 6px 6px 0; background: #FFF;'>" +
                        "        <div style='font-weight: bold; color: #2B6CB0; margin-bottom: 5px;'>⚡ Gestión de Energía</div>" +
                        "        No todas las horas del día valen lo mismo. Realiza tus tareas complejas (Estudio/Trabajo) en tus horas de mayor lucidez mental, y delega las tareas mecánicas (Hogar) para tus momentos de menor energía." +
                        "      </td>" +
                        "    </tr>" +
                        "  </table>" +

                        "  <div style='text-align: center; color: #A0AEC0; font-size: 10px; margin-top: 150px;'>Página 2 de 2</div>" +

                        "</body>" +
                        "</html>";

                // 4. invocar el motor de render nativo (WebView en segundo plano) para imprimir
                WebView webView = new WebView(requireContext());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        PrintManager printManager = (PrintManager) requireActivity().getSystemService(Context.PRINT_SERVICE);
                        String jobName = "Reporte_MindFocus_" + System.currentTimeMillis();
                        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

                        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                    }
                });
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
            });
        });
    }
    //metodos para calculos matematicos
    private double calcularPorcentajeFalla(int total, int completadas) {
        if (total == 0) return 0;
        return ((double)(total - completadas) * 100) / total;
    }
    private int calcularPorcentajeCompletado(int total, int completadas) {
        if (total == 0) return 0;
        return (completadas * 100) / total;
    }
    private String obtenerViewComoBase64(View view) {
        if (view == null || view.getWidth() == 0 || view.getHeight() == 0) {
            return "";
        }
        // 1. crear un bitmap del tamaño exacto de la vista del gráfico
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        // 2. comprimir el bitmap a formato PNG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        // 3. convertir a Base64 para incrustarlo en la etiqueta <img src="...">
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
    private void cargarReporte(){
        if (fechasSemanaStats.isEmpty()) return;// aun no se generan los chips
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("MindFocusPrefs", Context.MODE_PRIVATE);
        int usuarioId = prefs.getInt("usuarioId",-1);

        Calendar diaCal = fechasSemanaStats.get(diaSeleccionadoStats);
        long inicio = diaCal.getTimeInMillis();
        long fin = inicio + (24L * 60 * 60 * 1000) - 1;

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->{
            int total = db.tareaDao().contarTodasPorFecha(usuarioId, inicio, fin);
            int completas = db.tareaDao().contarCompletadasPorFecha(usuarioId, inicio, fin);
            // cantidad de tareas por cada tipo
            int estudio = db.tareaDao().totalEstudioPorFecha(usuarioId, inicio, fin);
            int trabajo = db.tareaDao().totalTrabajoPorFecha(usuarioId, inicio, fin);
            int hogar = db.tareaDao().totalHogarPorFecha(usuarioId, inicio, fin);
            int ejercicio = db.tareaDao().totalEjercicioPorFecha(usuarioId, inicio, fin);
            int creativo = db.tareaDao().totalCreativoPorFecha(usuarioId, inicio, fin);

            // cantidad de tareas por prioridad
            int alta = db.tareaDao().contarAltaPorFecha(usuarioId, inicio, fin);
            int media = db.tareaDao().contarMediaPorFecha(usuarioId, inicio, fin);
            int baja = db.tareaDao().contarBajaPorFecha(usuarioId, inicio, fin);

            //PROGRESO
            int totalEstudio = estudio;
            int estudioComp = db.tareaDao().estudioCompletadasPorFecha(usuarioId, inicio, fin);

            int totalEjercicio = ejercicio;
            int ejercicioComp = db.tareaDao().ejercicioCompletadasPorFecha(usuarioId, inicio, fin);

            int totalHogar = hogar;
            int hogarComp = db.tareaDao().hogarCompletadasPorFecha(usuarioId, inicio, fin);

            int totalTrabajo = trabajo;
            int trabajoComp = db.tareaDao().trabajoCompletadasPorFecha(usuarioId, inicio, fin);

            int totalCreativo = creativo;
            int creativoComp = db.tareaDao().creativoCompletadasPorFecha(usuarioId, inicio, fin);

            requireActivity().runOnUiThread(() ->{
                //-------------------------
                // Barra de progreso
                //-------------------------
                int porcentaje = 0;
                if(total>0)
                    porcentaje = completas*100/total;
                tvPorcentaje.setText("Progreso del día: "+porcentaje+"%");
                tvTotal.setText(String.valueOf(total));
                tvCompletadas.setText(String.valueOf("Llevas "+completas+" tareas completadas."));
                if (total == 0) {
                    tvProductividad.setText("Sin tareas");
                } else if(porcentaje>=80) {
                    tvProductividad.setText("Excelente");
                } else if(porcentaje>=50) {
                    tvProductividad.setText("Buena");
                } else {
                    tvProductividad.setText("Debe mejorar");
                }
                //-------------------------
                // DONUT
                //-------------------------
                List<DonutChartView.DonutSlice> datos = new ArrayList<>();
                // lista de secciones del grafico (se agrega color y cantidad)
                if (total == 0) {
                    // Si no hay tareas en el día, añadimos una rebanada gris neutra para el estado vacío
                    datos.add(new DonutChartView.DonutSlice(Color.parseColor("#E2E8F0"), 1));
                } else {
                    // Lista de secciones del gráfico si hay datos (se agrega color y cantidad)
                    if(estudio > 0)
                        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#4CAF50"), estudio)); // Verde
                    if(trabajo > 0)
                        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#2196F3"), trabajo)); // Azul
                    if(hogar > 0)
                        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#FF9800"), hogar));   // Amarillo
                    if(ejercicio > 0)
                        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#9C27B0"), ejercicio)); // Morado
                    if(creativo > 0)
                        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#F44336"), creativo));  // Rojo
                }
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
                        segEstudioCompletado,
                        segEstudioPendiente,
                        tvEstudioCantidad,
                        estudioComp,
                        totalEstudio
                );

                actualizarTipo(
                        segEjercicioCompletado,
                        segEjercicioPendiente,
                        tvEjercicioCantidad,
                        ejercicioComp,
                        totalEjercicio
                );

                actualizarTipo(
                        segHogarCompletado,
                        segHogarPendiente,
                        tvHogarCantidad,
                        hogarComp,
                        totalHogar
                );

                actualizarTipo(
                        segTrabajoCompletado,
                        segTrabajoPendiente,
                        tvTrabajoCantidad,
                        trabajoComp,
                        totalTrabajo
                );

                actualizarTipo(
                        segCreativoCompletado,
                        segCreativoPendiente,
                        tvCreativoCantidad,
                        creativoComp,
                        totalCreativo
                );

            });
        });
    }
    private void actualizarTipo(
            View segCompletado,
            View segPendiente,
            TextView cantidad,
            int completadas,
            int total
    ){
        cantidad.setText(completadas + "/" + total);
        int pendientes = total - completadas;
        // si no hay tareas en esta categoría, mostramos la barra completamente gris (neutra)
        if (total == 0) {
            setWeight(segCompletado, 0f);
            setWeight(segPendiente, 1f);
            return;
        }
        // reparte el ancho exactamente según la proporción real (no hace falta convertir a %,
        // el peso ya representa la proporción)
        setWeight(segCompletado, completadas);
        setWeight(segPendiente, pendientes);
    }
    //recargar
    @Override
    public void onResume() {
        super.onResume();
        if (!fechasSemanaStats.isEmpty()) {
            cargarReporte(); // ya no necesita regenerar los chips, solo recargar datos del día actual seleccionado
        }
    }
    private void setWeight(View view, float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }
    // genera los 7 chips y selecciona "hoy" por defecto
    private void generarChipsSemanaStats() {
        layoutDiasStats.removeAllViews();
        botonesDiasStats.clear();
        fechasSemanaStats.clear();

        Calendar hoy = Calendar.getInstance();

        Calendar pivotAux = (Calendar) semanaPivot.clone();
        int diaSemanaPivot = pivotAux.get(Calendar.DAY_OF_WEEK);
        int offsetHastaLunes = (diaSemanaPivot == Calendar.SUNDAY) ? -6 : -(diaSemanaPivot - Calendar.MONDAY);

        Calendar lunes = (Calendar) pivotAux.clone();
        lunes.add(Calendar.DAY_OF_MONTH, offsetHastaLunes);
        lunes.set(Calendar.HOUR_OF_DAY, 0);
        lunes.set(Calendar.MINUTE, 0);
        lunes.set(Calendar.SECOND, 0);
        lunes.set(Calendar.MILLISECOND, 0);

        int indiceSeleccionar = 0;//por defecto el lunes
        boolean estaEnSemanaActual = false;

        for (int i = 0; i < 7; i++) {
            Calendar diaCal = (Calendar) lunes.clone();
            diaCal.add(Calendar.DAY_OF_MONTH, i);
            fechasSemanaStats.add(diaCal);

            // si esta semana que estamos dibujando es la semana real actual, seleccionamos el día de "hoy"
            if (esMismoDia(diaCal, hoy)) {
                indiceSeleccionar = i;
                estaEnSemanaActual = true;
            }

            MaterialButton btnDia = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnDia.setText(nombresDias[i] + "\n" + diaCal.get(Calendar.DAY_OF_MONTH));
            btnDia.setTextSize(10);
            btnDia.setAllCaps(false);
            btnDia.setInsetTop(0);
            btnDia.setInsetBottom(0);
            btnDia.setPadding(0, 0, 0, 0);
            btnDia.setMinWidth(0);
            btnDia.setMinimumWidth(0);
            btnDia.setCornerRadius(dpToPx(10));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dpToPx(48), 1f);
            int margin = dpToPx(3);
            lp.setMarginStart(margin);
            lp.setMarginEnd(margin);
            btnDia.setLayoutParams(lp);

            final int index = i;
            btnDia.setOnClickListener(v -> seleccionarDiaStats(index));

            layoutDiasStats.addView(btnDia);
            botonesDiasStats.add(btnDia);
        }
        seleccionarDiaStats(indiceSeleccionar);
    }
    private void seleccionarDiaStats(int index) {
        diaSeleccionadoStats = index;
        for (int i = 0; i < botonesDiasStats.size(); i++) {
            MaterialButton b = botonesDiasStats.get(i);
            if (i == index) {
                b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.filtro1_activityTar));
                b.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            } else {
                b.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.bg2_tint_activityTar));
                b.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_mid));
            }
        }
        // actualiza el texto de fecha visible arriba
        Calendar diaCal = fechasSemanaStats.get(index);
        SimpleDateFormat sdfCompleto = new SimpleDateFormat("EEEE dd/MM/yyyy", new Locale("es"));
        tvSemana.setText(capitalize(sdfCompleto.format(diaCal.getTime())));
        cargarReporte();
    }
    private String capitalize(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);
    }
    private boolean esMismoDia(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}