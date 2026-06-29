package com.diacode.mindfocus;

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
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimeFragment extends Fragment {
    public TimeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mitiempo, container, false);
        DonutChartView donut = view.findViewById(R.id.donut_chart);
        List<DonutChartView.DonutSlice> datos = new ArrayList<>();
        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#FF6B6B"), 25));
        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#4ECDC4"), 30));
        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#45B7D1"), 20));
        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#F7B731"), 15));
        datos.add(new DonutChartView.DonutSlice(Color.parseColor("#9B59B6"), 10));
        donut.setSlices(datos);
        MaterialButton btn = view.findViewById(R.id.btnExportarPdf);
        btn.setOnClickListener(v -> exportPDF(view));
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

}