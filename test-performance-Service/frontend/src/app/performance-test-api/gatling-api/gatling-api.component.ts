import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { PerformanceTestApiService } from 'src/app/_services/performance-test-api.service';
import Swal from 'sweetalert2';
import { GatlingRequest, ResponseTimePerPercentile } from './gatling-request';
import { ApiResponse, GatlingAssertionResult, GatlingTestResult } from "../../models/gatlingTestResult";
import { GATLING_SCENARIOS } from "../../models/gatling-scenarios";

enum SIMULATION_STRATEGY {
  DEFAULT = "DEFAULT",
  SMOKE_TEST = "SMOKE_TEST",
  LOAD_TEST = "LOAD_TEST",
  STRESS_TEST = "STRESS_TEST",
  SPIKE_TEST = "SPIKE_TEST",
}

@Component({
  selector: 'app-gatling-api',
  templateUrl: './gatling-api.component.html',
  styleUrls: ['./gatling-api.component.css']
})
export class GatlingApiComponent implements OnInit {
  modal: HTMLElement | null = null;
  reportModal: HTMLElement | null = null;
  span: HTMLElement | null = null;
  testResult: any;

  gatlingTestResult: GatlingTestResult | null = null

  percentiles: number[] = [50, 75, 90, 95, 99, 99.9];
  newPercentile: number = 0;
  newResponseTime: number = 0;

  testLog: string = "";
  latestReportContent: SafeHtml | null = null; // Contenu du dernier rapport de test

  busy: Subscription | undefined;

  request: GatlingRequest = new GatlingRequest({});

  strategies: string[] = ["DEFAULT", "SMOKE_TEST", "LOAD_TEST", "STRESS_TEST", "SPIKE_TEST"];
  strategiesEnum = SIMULATION_STRATEGY;
  selectedStrategy: string = "DEFAULT";

  constructor(
    private readonly performanceTestApiService: PerformanceTestApiService,
    private sanitizer: DomSanitizer
  ) {
  }

  ngOnInit(): void {
    this.modal = document.getElementById("myModal");
    this.reportModal = document.getElementById("reportModal"); // Initialisation de la modal du rapport
    this.span = document.getElementsByClassName("close")[0] as HTMLElement;
  }

  validateForm(): boolean {
    let isValid = true;

    // Clear all previous errors
    const existingErrors = document.querySelectorAll('.text-danger');
    existingErrors.forEach(err => err.remove());
    const invalidInputs = document.querySelectorAll('.is-invalid');
    invalidInputs.forEach(input => input.classList.remove('is-invalid'));

    interface ValidationField {
      element: string;
      errorMessage: string;
      type: string;
      min?: number;
    }

    const requiredFields: ValidationField[] = [
      { element: 'testScenarioName', errorMessage: 'Veuillez entrer une valeur', type: 'text' },
      { element: 'testBaseUrl', errorMessage: 'Veuillez entrer une valeur', type: 'text' },
      { element: 'testUri', errorMessage: 'Veuillez entrer une valeur', type: 'text' },
      { element: 'testMethodType', errorMessage: 'Veuillez sélectionner un type de requête', type: 'text' }
    ];

    // Add strategy-specific fields
    if (this.selectedStrategy === SIMULATION_STRATEGY.DEFAULT || this.selectedStrategy === SIMULATION_STRATEGY.LOAD_TEST) {
      requiredFields.push({ element: 'testUsersNumber', errorMessage: 'Le nombre d\'utilisateurs doit être supérieur à 0', type: 'number', min: 1 });
      requiredFields.push({ element: 'testRampUpDuration', errorMessage: 'La durée de montée doit être supérieure ou égale à 0', type: 'number', min: 0 });
    } else if (this.selectedStrategy === SIMULATION_STRATEGY.STRESS_TEST) {
      requiredFields.push({ element: 'testUserRampUpPerSecondMin', errorMessage: 'Min users doit être >= 0', type: 'number', min: 0 });
      requiredFields.push({ element: 'testUserRampUpPerSecondMax', errorMessage: 'Max users doit être >= 1', type: 'number', min: 1 });
      requiredFields.push({ element: 'testUserRampUpPerSecondDuration', errorMessage: 'Durée doit être >= 1', type: 'number', min: 1 });
    } else if (this.selectedStrategy === SIMULATION_STRATEGY.SPIKE_TEST) {
      requiredFields.push({ element: 'testConstantUsers', errorMessage: 'Users constants doit être >= 0', type: 'number', min: 0 });
      requiredFields.push({ element: 'testConstantUsersDuration', errorMessage: 'Durée doit être >= 1', type: 'number', min: 1 });
      requiredFields.push({ element: 'testUsersAtOnce', errorMessage: 'At once users doit être >= 1', type: 'number', min: 1 });
    } else if (this.selectedStrategy === SIMULATION_STRATEGY.SMOKE_TEST) {
      requiredFields.push({ element: 'testUsersAtOnce', errorMessage: 'Users doit être >= 1', type: 'number', min: 1 });
    }

    requiredFields.forEach(field => {
      const inputElements = document.getElementsByName(field.element);
      if (!inputElements || inputElements.length === 0) return;

      const inputElement = inputElements[0] as HTMLInputElement;

      let isFieldInvalid = false;
      const value = inputElement.value;

      if (field.type === 'text' && (!value || value.trim() === '')) {
        isFieldInvalid = true;
      } else if (field.type === 'number') {
        const val = parseInt(value);
        if (isNaN(val) || (field.min !== undefined && val < field.min)) {
          isFieldInvalid = true;
        }
      }

      if (isFieldInvalid) {
        isValid = false;
        inputElement.classList.add('is-invalid');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'text-danger';
        errorDiv.innerText = field.errorMessage;
        inputElement.insertAdjacentElement('afterend', errorDiv);
      }
    });

    return isValid;
  }

  onSubmit() {
    if (!this.validateForm()) {
      return;
    }

    // Ensure simulationStrategy matches selectedStrategy
    this.request.simulationStrategy = this.selectedStrategy;

    console.log("Sending Gatling Request:", this.request);

    this.busy = this.performanceTestApiService.sendGatlingRequest(this.request)
      .subscribe({
        next: (response: ApiResponse) => {
          console.log("Gatling Response Received:", response);
          if (response.message && (response.message.startsWith('Error') || response.message.includes('failed to execute') || response.message.includes('Simulation failed'))) {
            Swal.fire({
              icon: 'error',
              title: 'Erreur de simulation',
              text: response.message,
              footer: 'Veuillez vérifier les logs serveur pour plus de détails.'
            });
            return;
          }

          this.testResult = response.testResult;
          const output = response.message || "";
          const hasReport = output.includes('Generated Report') || output.includes('Please open');

          if (this.testResult) {
            (this.testResult as any).reportGenerated = hasReport;
          }

          this.modal!.style.display = "block";
        },
        error: (error: any) => {
          console.error("Gatling Request Error:", error);
          let errorMessage = "Le test a échoué, révisez votre configuration de test";
          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            }
          } else if (error.message) {
            errorMessage = error.message;
          }

          Swal.fire({
            icon: 'error',
            title: 'Erreur Serveur (500)',
            text: errorMessage,
            footer: 'Assurez-vous que le backend est accessible et que la configuration est correcte.'
          });
        }
      });
  }

  //  Afficher le dernier rapport
  showLatestReport() {
    const url = this.performanceTestApiService.getLatestReportUrl();
    window.open(url, '_blank');
  }

  getLatestReportUrl(): string {
    return this.performanceTestApiService.getLatestReportUrl();
  }

  openReportModal() {
    if (this.reportModal) {
      this.reportModal.style.display = "block"; // Affiche la modal du rapport
      console.log("Report modal opened"); // Ajout de log pour vérifier l'ouverture du modal
    } else {
      console.error("reportModal is not initialized");
    }
  }

  closeReportModal() {
    if (this.reportModal) {
      this.reportModal.style.display = "none"; // Ferme la modal du rapport
      this.latestReportContent = null;
      console.log("Report modal closed"); // Ajout de log pour vérifier la fermeture du modal
    } else {
      console.error("reportModal is not initialized");
    }
  }

  closeModal() {
    if (this.modal) {
      this.modal.style.display = "none";
      this.latestReportContent = null;
      console.log("Modal closed"); // Ajout de log pour vérifier la fermeture du modal
    } else {
      console.error("modal is not initialized");
    }
  }

  newTest() {
    this.request = new GatlingRequest({});
    this.closeModal();
  }

  isSuccessfull(): boolean {
    if (!this.testResult || !this.testResult.assertions) {
      return false;
    }
    const failures = this.testResult.assertions.filter((assertion: GatlingAssertionResult) => assertion.result == false);
    console.log("Failures" + JSON.stringify(failures))
    return failures.length == 0;
  }

  onStrategySelect() {
    if (!this.selectedStrategy) {
      this.selectedStrategy = "DEFAULT";
    }

    const match = GATLING_SCENARIOS.find(scenario => scenario.name === this.selectedStrategy);
    if (match) {
      // Create a new GatlingRequest based on the template to ensure all properties exist
      this.request = new GatlingRequest(match.config);
    }
  }

  addPercentile(): void {
    console.log("HERE")
    console.log(`newPercentile: ${this.newPercentile} and newResponseTime: ${this.newResponseTime}`)

    if (this.newPercentile && this.newResponseTime) {

      console.log("HERE222")
      const newAssertion = new ResponseTimePerPercentile(this.newPercentile, this.newResponseTime);
      console.log("assertion:" + newAssertion.percentile + "% - " + newAssertion.responseTime + "ms")
      this.request.assertionsResponseTimePerPercentile.push(newAssertion);

      // Reset les champs
      this.newResponseTime = 0;
    }
  }

  removePercentile(index: number): void {
    this.request.assertionsResponseTimePerPercentile.splice(index, 1);
  }
}
