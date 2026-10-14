import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/** Export MOJ attachments: vector SVG for figures, upscaled PNG for forms. */
public class ExportTelecomAttachments {
    record Attachment(String id, String label, String url, boolean figure) {}

    static final int FORM_UPSCALE = 4;
    static final int VECTOR_DPI = 300;
    static final String ELECPE_RULE_PDF = "https://www.elecpe.org.tw/wp-content/uploads/groupdownload/Assurance/20-1.pdf";

    /** Vector PDF page sources (elecpe.org.tw) — MOJ scans are unreadable bitmaps. */
    static final Map<String, int[]> VECTOR_PAGES = Map.of(
        "att1", new int[] { 9 },
        "att2", new int[] { 10 },
        "att3", new int[] { 11 },
        "att4", new int[] { 12 }
    );

    static final Attachment[] ATTACHMENTS = {
        new Attachment("fig1", "\u9644\u5716\u4e00", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234795&lan=C", true),
        new Attachment("fig2", "\u9644\u5716\u4e8c", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234796&lan=C", true),
        new Attachment("fig3", "\u9644\u5716\u4e09", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234797&lan=C", true),
        new Attachment("fig4", "\u9644\u5716\u56db", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234798&lan=C", true),
        new Attachment("att1", "\u9644\u4ef6\u4e00\uff1a\u96fb\u4fe1\u5ba4\u9762\u7a4d\u4e00\u89bd\u8868", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234799&lan=C", false),
        new Attachment("att2", "\u9644\u4ef6\u4e8c\uff1a\u6d3d\u8fa6\uff0f\u5be9\u67e5\uff0f\u5be9\u9a57\u7533\u8acb\u8868", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234800&lan=C", false),
        new Attachment("att3", "\u9644\u4ef6\u4e09\uff1a\u6d3d\u8fa6\uff0f\u5be9\u67e5\uff0f\u5be9\u9a57\u4f5c\u696d\u6d41\u7a0b\u5716", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234801&lan=C", false),
        new Attachment("att4", "\u9644\u4ef6\u56db\uff1a\u96fb\u4fe1\u8a2d\u5099\u5be9\u5b9a\u8b49\u660e", "https://law.moj.gov.tw/LawClass/LawGetFile.ashx?FileId=0000234802&lan=C", false),
    };

    static void download(Path dest, String url) throws IOException {
        Files.createDirectories(dest.getParent());
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Downloaded " + dest.getFileName() + " (" + Files.size(dest) + " bytes)");
    }

    static List<String> exportFigureSvg(String id, Path outDir) throws IOException {
        Files.createDirectories(outDir);
        String svg = buildFigureSvg(id);
        Path out = outDir.resolve("page-001.svg");
        Files.writeString(out, svg, StandardCharsets.UTF_8);
        System.out.println("  -> vector SVG -> " + out);
        return List.of("assets/attachments/" + outDir.getFileName() + "/page-001.svg");
    }

    static String buildFigureSvg(String id) {
        String title = switch (id) {
            case "fig1" -> "\u9644\u5716\u4e00";
            case "fig2" -> "\u9644\u5716\u4e8c";
            case "fig3" -> "\u9644\u5716\u4e09";
            case "fig4" -> "\u9644\u5716\u56db";
            default -> id;
        };
        String[] boxes = switch (id) {
            case "fig1" -> new String[] {
                "\u5e02\u5167\u7db2\u8def\u696d\u52d9\u7d93\u71df\u8005\u8a2d\u7f6e\u65bc\u5efa\u7bc9\u7269\u7aef\u5b50\u677f\u4e4b\u96fb\u4ecb\u63a5\u7aef\u5b50",
                "\u7528\u6236\u5074\u7aef\u5b50\u677f\u4e4b\u96fb\u4ecb\u63a5\u7aef\u5b50",
                "\u7528\u6236\u81ea\u5099\u96fb\u4fe1\u7d42\u7aef\u8a2d\u5099",
            };
            case "fig2" -> new String[] {
                "\u5e02\u5167\u7db2\u8def\u696d\u52d9\u7d93\u71df\u8005\u8a2d\u7f6e\u65bc\u5efa\u7bc9\u7269\u7aef\u5b50\u677f\u4e4b\u96fb\u4ecb\u63a5\u7aef\u5b50",
                "\u7528\u6236\u81ea\u5099\u96fb\u4fe1\u7d42\u7aef\u8a2d\u5099",
            };
            case "fig3" -> new String[] {
                "\u5e02\u5167\u7db2\u8def\u696d\u52d9\u7d93\u71df\u8005\u8a2d\u7f6e\u65bc\u5efa\u7bc9\u7269\u5149\u7e6a\u914d\u7dda\u7bb1\u4e4b\u5149\u4ecb\u63a5\u7aef\u5b50",
                "\u7528\u6236\u5074\u5149\u7e6a\u914d\u7dda\u7bb1",
                "\u7528\u6236\u81ea\u5099\u96fb\u4fe1\u7d42\u7aef\u8a2d\u5099",
            };
            case "fig4" -> new String[] {
                "\u5e02\u5167\u7db2\u8def\u696d\u52d9\u7d93\u71df\u8005\u8a2d\u7f6e\u65bc\u5efa\u7bc9\u7269\u4e4b\u96fb\u4fe1\u8a2d\u5099\u5149\u6216\u96fb\u4ecb\u63a5\u7aef\u5b50",
                "\u7528\u6236\u81ea\u5099\u96fb\u4fe1\u7d42\u7aef\u8a2d\u5099",
            };
            default -> new String[0];
        };
        String wireLabel = switch (id) {
            case "fig1" -> "\u8df3\u7dda";
            case "fig2" -> "\u914d\u7dda\u7dda\u7e6a";
            case "fig3" -> "\u8df3\u63a5\u7dda";
            default -> null;
        };
        int demarcX = switch (id) {
            case "fig1" -> 430;
            case "fig2" -> 360;
            case "fig3" -> 430;
            case "fig4" -> 520;
            default -> 400;
        };
        int width = switch (id) {
            case "fig4" -> 860;
            default -> 920;
        };

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ").append(width).append(" 220\" ");
        sb.append("font-family=\"'Microsoft JhengHei','Noto Sans TC',sans-serif\" fill=\"#000\">\n");
        sb.append("<rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>\n");
        sb.append("<text x=\"12\" y=\"22\" font-size=\"16\" font-weight=\"700\">").append(esc(title)).append("</text>\n");
        sb.append("<text x=\"12\" y=\"58\" font-size=\"13\">\u2190 \u516c\u773e\u96fb\u4fe1\u7db2\u8def</text>\n");

        int[] xs = switch (boxes.length) {
            case 3 -> new int[] { 120, 500, 720 };
            case 2 -> new int[] { 120, 680 };
            default -> new int[] { 120 };
        };
        int[] ws = switch (boxes.length) {
            case 3 -> new int[] { 250, 170, 150 };
            case 2 -> new int[] { 300, 150 };
            default -> new int[] { 300 };
        };

        for (int i = 0; i < boxes.length; i++) {
            drawBox(sb, xs[i], 78, ws[i], 62, boxes[i]);
            if (i < boxes.length - 1) {
                int x1 = xs[i] + ws[i];
                int x2 = xs[i + 1];
                sb.append("<line x1=\"").append(x1).append("\" y1=\"109\" x2=\"").append(x2).append("\" y2=\"109\" stroke=\"#000\" stroke-width=\"1.5\"/>\n");
                sb.append("<polygon points=\"").append(x2 - 8).append(",104 ").append(x2).append(",109 ").append(x2 - 8).append(",114\" fill=\"#000\"/>\n");
            }
        }

        if (wireLabel != null) {
            sb.append("<text x=\"").append(demarcX - 28).append("\" y=\"98\" font-size=\"12\" text-anchor=\"middle\">")
              .append(esc(wireLabel)).append("</text>\n");
        }
        sb.append("<line x1=\"").append(demarcX).append("\" y1=\"72\" x2=\"").append(demarcX).append("\" y2=\"150\" stroke=\"#000\" stroke-width=\"1.2\" stroke-dasharray=\"6 4\"/>\n");
        sb.append("<text x=\"").append(demarcX).append("\" y=\"168\" font-size=\"12\" text-anchor=\"middle\">\u8cac\u4efb\u5206\u754c\u9ede</text>\n");
        sb.append("</svg>\n");
        return sb.toString();
    }

    static void drawBox(StringBuilder sb, int x, int y, int w, int h, String text) {
        sb.append("<rect x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(w)
          .append("\" height=\"").append(h).append("\" fill=\"#fff\" stroke=\"#000\" stroke-width=\"1.5\"/>\n");
        String[] lines = wrapText(text, 14);
        int lineHeight = 16;
        int startY = y + (h - lines.length * lineHeight) / 2 + 12;
        for (int i = 0; i < lines.length; i++) {
            sb.append("<text x=\"").append(x + w / 2).append("\" y=\"").append(startY + i * lineHeight)
              .append("\" font-size=\"13\" text-anchor=\"middle\">").append(esc(lines[i])).append("</text>\n");
        }
    }

    static String[] wrapText(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            cur.append(text.charAt(i));
            if (cur.length() >= maxChars) {
                lines.add(cur.toString());
                cur.setLength(0);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines.toArray(String[]::new);
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static List<String> exportFormPng(Path pdf, Path outDir, int upscale, String id, Path altPdfDir) throws Exception {
        if (VECTOR_PAGES.containsKey(id)) {
            Path vectorPdf = ensureVectorPdf(altPdfDir);
            return exportVectorPages(vectorPdf, VECTOR_PAGES.get(id), outDir, VECTOR_DPI);
        }
        Files.createDirectories(outDir);
        BufferedImage embedded = extractLargestImage(pdf);
        if (embedded == null) embedded = extractViaPdfimages(pdf, outDir);
        if (embedded == null) throw new IOException("No embedded image in " + pdf);
        System.out.println("  embedded " + embedded.getWidth() + "x" + embedded.getHeight());
        BufferedImage cropped = autoCrop(embedded);
        BufferedImage scaled = upscale(cropped, upscale);
        Path out = outDir.resolve("page-001.png");
        writePng(scaled, out);
        System.out.println("  -> upscaled x" + upscale + " -> " + scaled.getWidth() + "x" + scaled.getHeight());
        return List.of("assets/attachments/" + outDir.getFileName() + "/page-001.png");
    }

    static Path ensureVectorPdf(Path altPdfDir) throws IOException {
        Files.createDirectories(altPdfDir);
        Path pdf = altPdfDir.resolve("elecpe-20-1.pdf");
        if (!Files.exists(pdf) || Files.size(pdf) < 10000) {
            download(pdf, ELECPE_RULE_PDF);
        }
        return pdf;
    }

    static List<String> exportVectorPages(Path pdf, int[] pageNums, Path outDir, int dpi) throws Exception {
        Files.createDirectories(outDir);
        Path pdftoppm = findPdftoppm();
        if (pdftoppm == null) throw new IOException("pdftoppm not found");
        List<String> pages = new ArrayList<>();
        for (int i = 0; i < pageNums.length; i++) {
            int pageNum = pageNums[i];
            Path prefix = outDir.resolve("_render-" + pageNum);
            ProcessBuilder pb = new ProcessBuilder(
                pdftoppm.toString(), "-png", "-r", String.valueOf(dpi),
                "-f", String.valueOf(pageNum), "-l", String.valueOf(pageNum),
                "-singlefile", pdf.toString(), prefix.toString());
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                while (br.readLine() != null) {}
            }
            if (proc.waitFor() != 0) throw new IOException("pdftoppm failed page " + pageNum);
            Path rendered = Paths.get(prefix + ".png");
            if (!Files.exists(rendered)) throw new IOException("Missing " + rendered);
            String name = String.format("page-%03d.png", i + 1);
            Path out = outDir.resolve(name);
            Files.move(rendered, out, StandardCopyOption.REPLACE_EXISTING);
            pages.add("assets/attachments/" + outDir.getFileName() + "/" + name);
            System.out.println("  -> vector page " + pageNum + " @ " + dpi + " DPI -> " + name);
        }
        return pages;
    }

    static BufferedImage extractViaPdfimages(Path pdf, Path tmpDir) throws Exception {
        String pdfimages = "pdfimages";
        Path pdftoppm = findPdftoppm();
        if (pdftoppm != null) {
            Path candidate = pdftoppm.getParent().resolve("pdfimages.exe");
            if (Files.exists(candidate)) pdfimages = candidate.toString();
        }
        Path prefix = tmpDir.resolve("_extract");
        ProcessBuilder pb = new ProcessBuilder(pdfimages, "-png", pdf.toString(), prefix.toString());
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            while (br.readLine() != null) {}
        }
        if (proc.waitFor() != 0) return null;
        BufferedImage best = null;
        int bestArea = 0;
        try (var stream = Files.list(tmpDir)) {
            for (Path p : stream.filter(f -> f.getFileName().toString().startsWith("_extract")).toList()) {
                if (!p.toString().endsWith(".png")) continue;
                BufferedImage img = ImageIO.read(p.toFile());
                int area = img.getWidth() * img.getHeight();
                if (area > bestArea) { bestArea = area; best = img; }
                Files.deleteIfExists(p);
            }
        }
        return best;
    }

    static Path findPdftoppm() {
        String[] candidates = {
            "pdftoppm",
            "C:\\Program Files\\poppler\\Library\\bin\\pdftoppm.exe",
            "C:\\poppler\\Library\\bin\\pdftoppm.exe",
        };
        for (String c : candidates) {
            try {
                Process p = new ProcessBuilder(c, "-v").redirectErrorStream(true).start();
                p.getInputStream().transferTo(OutputStream.nullOutputStream());
                int code = p.waitFor();
                if (code == 0 || code == 99) return Paths.get(c);
            } catch (Exception ignored) {}
        }
        return null;
    }

    static BufferedImage extractLargestImage(Path pdf) throws IOException {
        BufferedImage best = null;
        int bestArea = 0;
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            for (PDPage page : doc.getPages()) {
                for (BufferedImage img : collectImages(page.getResources())) {
                    int area = img.getWidth() * img.getHeight();
                    if (area > bestArea) { bestArea = area; best = img; }
                }
            }
        }
        return best;
    }

    static List<BufferedImage> collectImages(PDResources res) throws IOException {
        List<BufferedImage> out = new ArrayList<>();
        if (res == null) return out;
        for (COSName name : res.getXObjectNames()) {
            PDXObject xo = res.getXObject(name);
            if (xo instanceof PDImageXObject img) {
                out.add(img.getImage());
            } else if (xo instanceof org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject form) {
                out.addAll(collectImages(form.getResources()));
            }
        }
        return out;
    }

    static BufferedImage autoCrop(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int minX = w, minY = h, maxX = 0, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
                if (r < 245 || g < 245 || b < 245) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX <= minX || maxY <= minY) return src;
        int pad = 4;
        minX = Math.max(0, minX - pad);
        minY = Math.max(0, minY - pad);
        maxX = Math.min(w - 1, maxX + pad);
        maxY = Math.min(h - 1, maxY + pad);
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    static BufferedImage upscale(BufferedImage src, int factor) {
        int w = src.getWidth() * factor;
        int h = src.getHeight() * factor;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform at = AffineTransform.getScaleInstance(factor, factor);
        g.drawImage(src, at, null);
        g.dispose();
        return sharpen(out, 1.2f);
    }

    static BufferedImage sharpen(BufferedImage src, float amount) {
        float[] kernel = {
            0, -amount, 0,
            -amount, 1 + 4 * amount, -amount,
            0, -amount, 0,
        };
        java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(
            new java.awt.image.Kernel(3, 3, kernel),
            java.awt.image.ConvolveOp.EDGE_NO_OP, null);
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        op.filter(src, dst);
        return dst;
    }

    static void writePng(BufferedImage image, Path out) throws IOException {
        BufferedImage rgb = image.getType() == BufferedImage.TYPE_INT_RGB ? image : copyToRgb(image);
        ImageIO.write(rgb, "png", out.toFile());
    }

    static BufferedImage copyToRgb(BufferedImage image) {
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rgb;
    }

    static String jsStr(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                default: sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("C:\\Users\\jalin.you\\AJ-WORK\\telecom-law-web");
        boolean forceDownload = false;
        for (String arg : args) {
            if (arg.equals("--force")) forceDownload = true;
            else if (!arg.startsWith("-")) root = Paths.get(arg);
        }

        Path pdfDir = root.resolve("scripts/source-pdf");
        Path altPdfDir = root.resolve("scripts/alt-pdf");
        Path attachRoot = root.resolve("assets/attachments");
        Files.createDirectories(pdfDir);

        StringBuilder attJs = new StringBuilder();
        attJs.append("const ATTACHMENTS = [\n");

        for (Attachment a : ATTACHMENTS) {
            Path pdf = pdfDir.resolve(a.id() + ".pdf");
            if (forceDownload || !Files.exists(pdf) || Files.size(pdf) < 1000) {
                download(pdf, a.url());
            }
            Path outDir = attachRoot.resolve(a.id());
            List<String> pages = a.figure()
                ? exportFigureSvg(a.id(), outDir)
                : exportFormPng(pdf, outDir, FORM_UPSCALE, a.id(), altPdfDir);
            attJs.append("  {\n");
            attJs.append("    id: ").append(jsStr(a.id())).append(",\n");
            attJs.append("    label: ").append(jsStr(a.label())).append(",\n");
            attJs.append("    mojUrl: ").append(jsStr(a.url())).append(",\n");
            attJs.append("    pages: [\n");
            for (String p : pages) {
                attJs.append("      ").append(jsStr(p)).append(",\n");
            }
            attJs.append("    ],\n");
            attJs.append("  },\n");
        }
        attJs.append("];\n");

        Path dataJs = root.resolve("js/data.js");
        String data = Files.readString(dataJs, StandardCharsets.UTF_8);
        int start = data.indexOf("const ATTACHMENTS = [");
        int end = data.indexOf("];", start);
        if (start < 0 || end < 0) throw new RuntimeException("ATTACHMENTS block not found in data.js");
        end += 3;
        String updated = data.substring(0, start) + attJs + data.substring(end);
        Files.writeString(dataJs, updated, StandardCharsets.UTF_8);
        System.out.println("Updated " + dataJs);
    }
}
