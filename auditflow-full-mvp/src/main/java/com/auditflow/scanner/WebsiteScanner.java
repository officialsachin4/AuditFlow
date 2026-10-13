package com.auditflow.scanner;

import com.auditflow.model.Audit;
import com.auditflow.model.Finding;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class WebsiteScanner
{
    public Audit scan(String raw)
    {
        String url = normalize(raw);
        Audit a = new Audit(url);
        R acc = accessibility(a, url),
                sec = security(a, url),
                seo = seo(a, url),
                priv = privacy(a, url),
                perf = performance(a, url),
                tech = technical(a, url),
                mob = mobile(a, url);

        a.accessibilityScore = score(acc);
        a.securityScore = score(sec);
        a.seoScore = score(seo);
        a.privacyScore = score(priv);
        a.performanceScore = score(perf);
        a.technicalScore = score(tech);
        a.mobileScore = score(mob);

        a.overallScore = (int) Math.round(
                a.securityScore * .20 + a.performanceScore * .20 + a.accessibilityScore * .15 +
                        a.seoScore * .15 + a.privacyScore * .10 + a.technicalScore * .10 + a.mobileScore * .10);
        return a;
    }

    int score(R r)
    {
        return r.total == 0 ? 100 : (int) Math.round(100.0 * r.pass / r.total);
    }

    void f(Audit a, String c, String s, String t, String d, String rec)
    {
        a.findings.add(new Finding(c, s, t, d, rec));
    }

    record R(int total, int pass)
    {

    }

    String normalize(String u)
    {
        u = u.trim();
        return u.matches("(?i)^https?://.*") ? u : "https://" + u;
    }


    HttpResponse<String> get(String u) throws Exception
    {
        return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15)).build()
                .send(HttpRequest.newBuilder(URI.create(u)).timeout(Duration.ofSeconds(30))
                                .header("User-Agent", "AuditFlowBot/1.0").GET().build(),
                        HttpResponse.BodyHandlers.ofString());
    }

    Document doc(String u) throws Exception
    {
        return Jsoup.parse(get(u).body(), u);
    }

    R accessibility(Audit a, String u)
    {
        try {
            Document d = doc(u);
            int t = 0, p = 0;
            t++;
            if (!d.title().isBlank()) p++;
            else
                f(a, "Accessibility", "HIGH", "Missing page title", "No title element/text was found.", "Add a unique descriptive <title>.");
            f(a, "Accessibility", "MEDIUM", "Missing language attribute", "The root HTML element has no lang attribute.", "Add a language attribute such as <html lang=\"en\">.");
            for (Element e : d.select("img")) {
                t++;
                if (e.hasAttr("alt")) p++;
                else
                    f(a, "Accessibility", "MEDIUM", "Image missing alt text", "An image has no alt attribute.", "Add meaningful alt text or alt=\"\" for decorative images.");
            }
            for (Element e : d.select("input,select,textarea")) {
                if (e.attr("type").equalsIgnoreCase("hidden")) continue;
                t++;
                String id = e.id();
                boolean ok = !id.isBlank() && !d.select("label[for=" + css(id) + "]").isEmpty() || e.parent().tagName().equals("label");
                if (ok) p++;
                else
                    f(a, "Accessibility", "MEDIUM", "Form control may lack label", "A form control has no obvious associated label.", "Associate it with a <label>.");
            }
            return new R(t, p);
        } catch (Exception e) {
            f(a, "Accessibility", "HIGH", "Page could not be parsed", e.getMessage(), "Ensure the URL is reachable.");
            return new R(1, 0);
        }
    }

    R security(Audit a, String u)
    {
        try {
            var r = get(u);
            int t = 0, p = 0;
            t++;
            if (u.toLowerCase().startsWith("https://")) p++;
            else f(a, "Security", "CRITICAL", "HTTPS not used", "The target URL is not HTTPS.", "Use HTTPS.");
            String[] hs = {"strict-transport-security", "content-security-policy", "x-content-type-options", "referrer-policy", "permissions-policy"};
            String[] names = {"HSTS", "Content Security Policy", "X-Content-Type-Options", "Referrer-Policy", "Permissions-Policy"};
            for (int i = 0; i < hs.length; i++) {
                t++;
                if (r.headers().firstValue(hs[i]).isPresent()) p++;
                else
                    f(a, "Security", i < 2 ? "HIGH" : "MEDIUM", "Missing " + names[i], "The response does not contain " + names[i] + ".", "Configure the " + names[i] + " response header.");
            }
            String set = r.headers().firstValue("set-cookie").orElse("");
            t++;
            if (set.isBlank() || set.toLowerCase().contains("secure")) p++;
            else
                f(a, "Security", "MEDIUM", "Cookie may lack Secure flag", "A Set-Cookie header does not show Secure.", "Mark sensitive cookies Secure.");
            return new R(t, p);
        } catch (Exception e) {
            f(a, "Security", "CRITICAL", "Security scan failed", e.getMessage(), "Ensure the server is reachable.");
            return new R(1, 0);
        }
    }

    R seo(Audit a, String u)
    {
        try {
            Document d = doc(u);
            int t = 0, p = 0;
            t++;
            if (!d.title().isBlank() && d.title().length() <= 65) p++;
            else
                f(a, "SEO", "HIGH", "Title missing or too long", "The title is missing or exceeds a common search snippet length.", "Add a concise unique title.");
            t++;
            String md = d.select("meta[name=description]").attr("content");
            if (!md.isBlank() && md.length() <= 170) p++;
            else
                f(a, "SEO", "HIGH", "Meta description missing or too long", "No suitable meta description was found.", "Add a concise unique meta description.");
            t++;
            if (!d.select("link[rel=canonical]").isEmpty()) p++;
            else f(a, "SEO", "MEDIUM", "Canonical URL missing", "No canonical link was found.", "Add a canonical URL.");
            t++;
            if (d.select("h1").size() == 1) p++;
            else
                f(a, "SEO", "MEDIUM", "H1 structure issue", "Expected exactly one H1 on the page.", "Use one clear primary H1.");
            t++;
            if (!d.select("meta[property=og:title]").isEmpty()) p++;
            else
                f(a, "SEO", "LOW", "Open Graph title missing", "og:title was not detected.", "Add Open Graph metadata.");
            t++;
            if (!d.select("script[type=application/ld+json]").isEmpty()) p++;
            else
                f(a, "SEO", "LOW", "Structured data missing", "No JSON-LD structured data was detected.", "Add relevant Schema.org JSON-LD where appropriate.");
            return new R(t, p);
        } catch (Exception e) {
            return new R(1, 0);
        }
    }

    R privacy(Audit a, String u)
    {
        try {
            Document d = doc(u);
            int t = 0, p = 0;
            String text = d.text().toLowerCase(), html = d.html().toLowerCase();
            t++;
            if (text.contains("privacy policy") || !d.select("a[href*=privacy]").isEmpty()) p++;
            else
                f(a, "Privacy", "MEDIUM", "Privacy policy signal missing", "No obvious privacy policy link/text was found.", "Publish and link an appropriate privacy policy.");
            t++;
            if (text.contains("cookie policy") || !d.select("a[href*=cookie]").isEmpty()) p++;
            else
                f(a, "Privacy", "MEDIUM", "Cookie policy signal missing", "No obvious cookie policy link/text was found.", "Provide appropriate cookie information where required.");
            t++;
            if (!html.contains("googletagmanager") && !html.contains("google-analytics") && !html.contains("facebook.net"))
                p++;
            else
                f(a, "Privacy", "LOW", "Common tracker detected", "Common analytics/marketing scripts were detected.", "Verify consent and privacy configuration as applicable.");
            return new R(t, p);
        } catch (Exception e) {
            return new R(1, 0);
        }
    }

    R performance(Audit a, String u) {
        try {
            var r = get(u);
            int t = 0, p = 0;
            t++;
            if (r.statusCode() >= 200 && r.statusCode() < 400) p++;
            else
                f(a, "Performance", "HIGH", "Unsuccessful HTTP response", "HTTP status: " + r.statusCode(), "Return a successful response.");
            t++;
            long bytes = r.body().getBytes().length;
            if (bytes <= 500_000) p++;
            else
                f(a, "Performance", "MEDIUM", "Large HTML response", "HTML is approximately " + bytes + " bytes.", "Reduce HTML size and unnecessary markup.");
            return new R(t, p);
        } catch (Exception e) {
            return new R(1, 0);
        }
    }

    R technical(Audit a, String u) {
        try {
            Document d = doc(u);
            int t = 0, p = 0;
            t++;
            if (!d.select("meta[charset]").isEmpty()) p++;
            else
                f(a, "Technical", "MEDIUM", "Character encoding declaration missing", "No charset meta tag was detected.", "Declare UTF-8.");
            t++;
            if (!d.select("meta[name=viewport]").isEmpty()) p++;
            else
                f(a, "Technical", "MEDIUM", "Viewport meta missing", "No viewport meta tag was detected.", "Add a responsive viewport declaration.");
            t++;
            if (!d.select("link[rel=icon],link[rel=shortcut icon]").isEmpty()) p++;
            else
                f(a, "Technical", "LOW", "Favicon not detected", "No common favicon link was found.", "Add a favicon.");
            t++;
            if (d.select("meta[name=robots]").isEmpty() || !d.select("meta[name=robots]").attr("content").toLowerCase().contains("noindex"))
                p++;
            else
                f(a, "Technical", "HIGH", "Page contains noindex", "The page instructs crawlers not to index it.", "Remove noindex if the page should rank.");
            return new R(t, p);
        } catch (Exception e) {
            return new R(1, 0);
        }
    }

    R mobile(Audit a, String u) {
        try {
            Document d = doc(u);
            int t = 0, p = 0;
            t++;
            if (d.select("meta[name=viewport]").attr("content").toLowerCase().contains("width=device-width")) p++;
            else
                f(a, "Mobile", "HIGH", "Mobile viewport not configured", "Responsive viewport configuration was not detected.", "Use width=device-width and an appropriate scale.");
            t++;
            if (!d.select("img[srcset],picture source[srcset]").isEmpty()) p++;
            else
                f(a, "Mobile", "LOW", "Responsive image source not detected", "No srcset/picture responsive image source was found.", "Consider responsive image sources.");
            return new R(t, p);
        } catch (Exception e) {
            return new R(1, 0);
        }
    }

    String css(String s)
    {
        return s.replace("\\", "\\\\").replace(":", "\\:").replace(".", "\\.");
    }


}