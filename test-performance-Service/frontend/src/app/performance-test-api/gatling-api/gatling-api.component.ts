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

  strategies: string[] = Object.keys(SIMULATION_STRATEGY).filter(k => isNaN(Number(k)));
  strategiesEnum = SIMULATION_STRATEGY;
  selectedStrategy: string | null = null;

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
    const requiredFields = [
      { element: 'testScenarioName', errorMessage: 'Veuillez entrer une valeur', type: 'text' },
      { element: 'testBaseUrl', errorMessage: 'Veuillez entrer une valeur', type: 'text' },
      { element: 'testUri', errorMessage: 'Veuillez entrer une valeur', type: 'text' },
      { element: 'testMethodType', errorMessage: 'Veuillez sélectionner un type de requête', type: 'text' },
      { element: 'testUsersNumber', errorMessage: 'Le nombre d\'utilisateurs doit être supérieur à 0', type: 'number', min: 1 },
      { element: 'testRampUpDuration', errorMessage: 'La durée de montée doit être supérieure ou égale à 0', type: 'number', min: 0 }
    ];

    requiredFields.forEach(field => {
      const inputElement = document.getElementsByName(field.element)[0] as HTMLInputElement | null;
      if (!inputElement) return;

      const errorDiv = document.createElement('div');
      errorDiv.className = 'text-danger';

      if (inputElement.nextElementSibling?.className === 'text-danger') {
        inputElement.nextElementSibling.remove();
      }

      let isFieldInvalid = false;
      if (field.type === 'text' && inputElement.value.trim() === '') {
        isFieldInvalid = true;
      } else if (field.type === 'number') {
        const val = parseInt(inputElement.value);
        if (isNaN(val) || (field.min !== undefined && val < field.min)) {
          isFieldInvalid = true;
        }
      }

      if (isFieldInvalid) {
        isValid = false;
        inputElement.classList.add('is-invalid');
        errorDiv.innerText = field.errorMessage;
        inputElement.insertAdjacentElement('afterend', errorDiv);
      } else {
        inputElement.classList.remove('is-invalid');
      }
    });

    return isValid;
  }

  onSubmit() {

    if (!this.validateForm()) {
      return;
    }

    this.busy = this.performanceTestApiService.sendGatlingRequest(this.request)
      .subscribe((response: any) => {
        if (response.message && response.message.startsWith('Error')) {
          Swal.fire({
            icon: 'error',
            title: 'Erreur de simulation',
            text: response.message,
          });
          return;
        }
        this.modal!.style.display = "block";

        // Extraction des messages de la réponse de l'API
        const pattern = /(.+)\n?/g;
        const matches: string[] = Array.from(response.message?.matchAll(pattern));
        const arrayOfStrings = matches.map(match => match[0]);

        // Filtrer les messages pour ne conserver que ceux indiquant le succès ou l'échec global
        const successMessage = arrayOfStrings.find(message => message.includes('request count'));
        const reportGeneratedMessage = arrayOfStrings.find(message => message.includes('Generated Report'));

        // Détermination du succès ou de l'échec global
        this.testResult = [{
          success: !!successMessage
        }];

        this.testResult = (response as ApiResponse).testResult

        // Ajouter un message indiquant que le rapport a été généré
        if (reportGeneratedMessage) {
          this.testResult.push({
            message: 'Le rapport a été généré avec succès.',
            success: true
          });
        }

      }, (error: any) => {
        let errorMessage = "Le test a échoué, révisez votre configuration de test";
        if (error.error && typeof error.error === 'string') {
          errorMessage = error.error;
        } else if (error.message) {
          errorMessage = error.message;
        }
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: errorMessage,
        })
      });
  }

  //  Afficher le dernier rapport
  showLatestReport() {
    const reportWindow = window.open(this.performanceTestApiService.getLatestReportUrl().toString(), '_blank');
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
    const failures = this.testResult.assertions.filter((assertion: GatlingAssertionResult) => assertion.result == false);
    console.log("Failures" + failures)
    return failures.length == 0;
  }

  onStrategySelect() {
    if (this.selectedStrategy == null)
      this.selectedStrategy = SIMULATION_STRATEGY.DEFAULT.valueOf()

    if (this.strategies.includes(this.selectedStrategy))
      this.request = GATLING_SCENARIOS.filter(scenario => scenario.name == this.selectedStrategy).map(it => it.config)[0]
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
