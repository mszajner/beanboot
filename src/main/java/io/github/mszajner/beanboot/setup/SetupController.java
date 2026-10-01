package io.github.mszajner.beanboot.setup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
class SetupController {

    private final SetupService setupService;
    private final ConfigurableApplicationContext applicationContext;

    /** Root redirect – during wizard mode all paths lead to /setup. */
    @GetMapping("/")
    public String root() {
        return "redirect:/setup";
    }

    @GetMapping("/setup")
    public String showSetup(Model model) {
        model.addAttribute("form", new SetupForm());
        return "setup/index";
    }

    @PostMapping("/setup")
    public String processSetup(@ModelAttribute SetupForm form, Model model) {
        try {
            setupService.testAndSave(form);
            // Close the wizard context after the response has been sent.
            // A short delay ensures the "complete" page reaches the browser first.
            new Thread(() -> {
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                log.info("Setup complete – shutting down wizard context");
                applicationContext.close();
            }, "setup-shutdown").start();
            return "setup/complete";
        } catch (Exception e) {
            log.warn("Database connection test failed: {}", e.getMessage(), e);
            model.addAttribute("form", form);
            model.addAttribute("error", e.getMessage());
            return "setup/index";
        }
    }
}
