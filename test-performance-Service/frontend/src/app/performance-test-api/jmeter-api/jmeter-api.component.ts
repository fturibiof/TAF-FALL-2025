import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { JMeterHttpRequest } from './jmeter-http-request';
import { JMeterFTPRequest } from './jmeter-ftp-request';
import { JMETER_SCENARIOS } from '../../models/jmeter_scenarios';

import { Subscription } from 'rxjs';
import Swal from 'sweetalert2';
import { PerformanceTestApiService } from 'src/app/_services/performance-test-api.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-jmeter-api',
  templateUrl: './jmeter-api.component.html',
  styleUrls: [
    '../gatling-api/gatling-api.component.css',
    './jmeter-api.component.css',
  ],
})
export class JmeterApiComponent implements OnInit {

  isHttpSidebarVisible: boolean = false;
  isFtpSidebarVisible: boolean = false;
  showHttpButton: boolean = true;
  showFtpButton: boolean = true;

  modal: HTMLElement | null = document.getElementById('myModal');
  span: Element | null = document.getElementsByClassName('close')[0];
  testResult: any;
  testLog: String = '';
  reportFilePath: String = '';
  busy: Subscription | undefined;

  http_request: JMeterHttpRequest = new JMeterHttpRequest();
  ftp_request: JMeterFTPRequest = new JMeterFTPRequest();
  requestTargets: { [type: string]: any } = {
    'http': this.http_request,
    'ftp': this.ftp_request,
  };

  formType: string = 'http';
  isGlossaryVisible: boolean = false;

  filteredScenarios: any[] = [];
  selectedScenario: string | null = null;
  scenarioConfigurations = JMETER_SCENARIOS;

  http_description = document.getElementById('http-description');
  ftp_description = document.getElementById('ftp-description');

  testResults: any[] = [];
  result_table: HTMLElement | null = document.getElementById('result_table');
  httpForm: HTMLElement | null = document.getElementById('http-form');
  ftpForm: HTMLElement | null = document.getElementById('ftp-form');
  switchLabel: HTMLElement | null = document.getElementById('switchLabel');
  switchCheckbox: HTMLInputElement | null = document.getElementById(
    'formSwitch'
  ) as HTMLInputElement;

  selectedTest: any = null;


  constructor(
    private fb: FormBuilder,
    private performanceTestApiService: PerformanceTestApiService) { }

  ngOnInit(): void {
    this.modal = document.getElementById('myModal');
    this.span = document.getElementsByClassName('close')[0];
    this.result_table = document.getElementById('result_table');
    this.httpForm = document.getElementById('http-form');
    this.ftpForm = document.getElementById('ftp-form');
    this.switchLabel = document.getElementById('switchLabel');
    this.http_description = document.getElementById('http-description');
    this.ftp_description = document.getElementById('ftp-description');

    this.switchCheckbox = document.getElementById(
      'formSwitch'
    ) as HTMLInputElement;

    this.updateButtonVisibility();
    this.updateScenariosFilter();

    // Set default methods
    this.http_request.method = 'GET';
    this.ftp_request.method = 'Retrieve';
    this.http_request.protocol = 'HTTP';
  }

  toggleHttpSidebar() {
    this.isHttpSidebarVisible = !this.isHttpSidebarVisible;
    this.adjustFormMargin();
  }

  toggleFtpSidebar() {
    this.isFtpSidebarVisible = !this.isFtpSidebarVisible;
    this.adjustFormMargin();
  }

  adjustFormMargin() {
    const mainContent = document.querySelector('.main') as HTMLElement;
    const headerContainer = document.querySelector('.header-container') as HTMLElement;

    if (mainContent && headerContainer) {
      if (this.isHttpSidebarVisible || this.isFtpSidebarVisible) {
        mainContent.style.marginLeft = '500px';
        headerContainer.style.marginLeft = '500px';
      } else {
        mainContent.style.marginLeft = '0';
        headerContainer.style.marginLeft = '0';
      }
    }
  }

  resetForms() {
    const httpForm = document.getElementById('http-form') as HTMLFormElement;
    const ftpForm = document.getElementById('ftp-form') as HTMLFormElement;

    if (httpForm) {
      httpForm.reset();
    }
    if (ftpForm) {
      ftpForm.reset();
    }
  }

  updateButtonVisibility() {
    if (this.switchCheckbox?.checked) {
      this.showHttpButton = false;
      this.showFtpButton = true;
    } else {
      this.showHttpButton = true;
      this.showFtpButton = false;
    }
  }

  toggleForms() {
    this.isHttpSidebarVisible = false;
    this.isFtpSidebarVisible = false;
    this.adjustFormMargin();

    const httpForm = document.getElementById('http-form') as HTMLFormElement;
    const ftpForm = document.getElementById('ftp-form') as HTMLFormElement;
    const switchLabel = document.getElementById('switchLabel') as HTMLElement;

    if (this.switchCheckbox?.checked) {
      // Show FTP form
      httpForm.style.display = 'none';
      ftpForm.style.display = 'block';
      this.showHttpButton = false;
      this.showFtpButton = true;
      switchLabel.innerText = 'FTP';
    } else {
      // Show HTTP form
      httpForm.style.display = 'block';
      ftpForm.style.display = 'none';
      this.showHttpButton = true;
      this.showFtpButton = false;
      switchLabel.innerText = 'HTTP';
    }

    console.log('Switch checked:', this.switchCheckbox?.checked);
    this.formType = this.switchCheckbox?.checked ? 'ftp' : 'http';

    this.resetForms();
    this.updateButtonVisibility();
    this.updateScenariosFilter();
  }

  updateScenariosFilter() {
    this.filteredScenarios = JMETER_SCENARIOS.filter(
      scenario => scenario.type === this.formType
    );
    console.log('Filtering scenarios for:', this.formType);
    console.log('Results:', this.filteredScenarios);
  }

  onScenarioSelect() {
    const scenario = this.scenarioConfigurations.find(
      s => s.name === this.selectedScenario
    );
    if (!scenario) return;

    const target = this.requestTargets[scenario.type];
    this.applyScenario(target, scenario.config);
  }

  applyScenario<T>(target: T, source: Partial<T>): T {
    Object.keys(source).forEach(key => {
      const configKey = key as keyof T;
      const sourceValue = source[configKey];

      if (typeof sourceValue === 'string' && sourceValue !== '') {
        target[configKey] = sourceValue as T[keyof T];
      }
      else if (sourceValue !== null && sourceValue !== undefined) {
        target[configKey] = sourceValue as T[keyof T];
      }
    });
    return target;
  }

  onProtocolChange() {
    if (this.http_request.protocol === 'FTP') {
      // Toggle to FTP form
      if (this.switchCheckbox) {
        this.switchCheckbox.checked = true;
        this.toggleForms();
      }
    }
  }

  toggleGlossary() {
    this.isGlossaryVisible = !this.isGlossaryVisible;
  }

  selectTestType(type: string) {
    const target = this.formType === 'http' ? this.http_request : this.ftp_request;

    switch (type) {
      case 'smoke':
        target.nbThreads = '5';
        target.rampTime = '1';
        target.duration = '60';
        target.loop = '1';
        break;
      case 'load':
        target.nbThreads = '50';
        target.rampTime = '10';
        target.duration = '300';
        target.loop = '1';
        break;
      case 'stress':
        target.nbThreads = '200';
        target.rampTime = '20';
        target.duration = '600';
        target.loop = '1';
        break;
      case 'spike':
        target.nbThreads = '500';
        target.rampTime = '5';
        target.duration = '120';
        target.loop = '1';
        break;
    }
  }

  validateHttpForm(): boolean {
    let isValid = true;
    const requiredFields = [
      { element: 'loop', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'nbThreads', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'domain', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'path', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'methodType', errorMessage: 'Veuillez sélectionner un type de requête' },
      // { element: 'duration', errorMessage: 'Veuillez entrer une durée' }, // si lors d'un nouveau teste il est requis
      // { element: 'rampTime', errorMessage: 'Veuillez entrer un temps de montée' }, // lors d'un nouveau teste il est requis
      { element: 'protocol', errorMessage: 'Veuillez entrer un protocole' } // lors d'un nouveau teste il est requis
    ];

    requiredFields.forEach(field => {
      const inputElement = document.getElementsByName(field.element)[0] as HTMLInputElement | null;
      const errorDiv = document.createElement('div');
      errorDiv.className = 'text-danger';

      if (inputElement?.nextElementSibling) {
        inputElement.nextElementSibling.remove();
      }
      if (inputElement && inputElement.value.trim() === '') {
        isValid = false;
        inputElement.classList.add('is-invalid');
        errorDiv.innerText = field.errorMessage;
        inputElement.insertAdjacentElement('afterend', errorDiv);
      } else {
        inputElement?.classList.remove('is-invalid');
      }
    });

    return isValid;
  }

  validateFtpForm(): boolean {
    let isValid = true;

    const requiredFields = [
      { element: 'loopFtp', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'nbThreadsFtp', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'domainFtp', errorMessage: 'Veuillez entrer une valeur' },
      { element: 'methodTypeFtp', errorMessage: 'Veuillez sélectionner un type de requête' }
    ];

    requiredFields.forEach(field => {
      const inputElement = document.getElementsByName(field.element)[0] as HTMLInputElement | null;
      const errorDiv = document.createElement('div');
      errorDiv.className = 'text-danger';

      if (inputElement?.nextElementSibling) {
        inputElement.nextElementSibling.remove();
      }

      if (inputElement && inputElement.value.trim() === '') {
        isValid = false;
        inputElement.classList.add('is-invalid');
        errorDiv.innerText = field.errorMessage;
        inputElement.insertAdjacentElement('afterend', errorDiv);
      } else {
        inputElement?.classList.remove('is-invalid');
      }
    });

    return isValid;
  }

  onHttpSubmit(showAlert: boolean = false) {
    if (!this.validateHttpForm()) {
      return;
    }

    this.busy = this.performanceTestApiService
      .sendHttpJMeterRequest(this.http_request)
      .subscribe((response: any) => {
        this.testResults = response;

        // Transformation de la réponse pour inclure des informations sur le succès ou l'échec global
        const successMessage = response.length != 0;
        this.testResult = [{
          success: successMessage,
          details: response.details // Assurez-vous que les détails sont inclus dans la réponse
        }];

        // Ajouter un message indiquant que le rapport a été généré
        if (successMessage) {
          this.testResult.push({
            message: 'Le rapport a été généré avec succès.',
            success: true
          });
        }

        if (successMessage) {
          this.modal!.style.display = 'block';
        } else {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: "Le test a échoué, révisez votre configuration de test",
          })
        }
      }, (error: any) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: "Le test a échoué, révisez votre configuration de test",
        })
      });
  }

  onFtpSubmit() {
    if (!this.validateFtpForm()) {
      return;
    }

    this.busy = this.performanceTestApiService
      .sendFtpJMeterRequest(this.ftp_request)
      .subscribe((response: any) => {
        this.testResults = response;
        this.testResult = response; // Ajoutez cette ligne
        if (response.length != 0) {
          this.modal!.style.display = 'block';
        } else {
          Swal.fire({
            icon: 'error',
            title: 'Erreur',
            text: "Le test a échoué, révisez votre configuration de test",
          })
        }

      }, (error: any) => {
        Swal.fire({
          icon: 'error',
          title: 'Erreur',
          text: "Le test a échoué, révisez votre configuration de test",
        })
      });
  }

  closeModal() {
    this.modal!.style.display = 'none';
  }

  showTestDetails(test: any) {
    console.log("Test details:", test);
    this.selectedTest = test;
  }

  closeTestDetails() {
    this.selectedTest = null;
  }

  closeModalOnOutsideClick(event: MouseEvent) {
    if ((event.target as HTMLElement).id === 'detailModal') {
      this.closeTestDetails();
    }
  }

  newTest() {
    this.testResults = [];
    this.selectedTest = null;
    this.modal!.style.display = 'none';
    if (this.httpForm) {
      (this.httpForm as HTMLFormElement).reset();
    }
    if (this.ftpForm) {
      (this.ftpForm as HTMLFormElement).reset();
    }
  }

  //  Afficher le dernier rapport
  showLatestReport() {
    if (this.testResult && this.testResult.length > 0) {
      const reportUrl = `${environment.apiUrl}${this.testResult[0].details['location-url']}`;
      window.open(reportUrl, '_blank');
    } else {
      Swal.fire({
        icon: 'error',
        title: 'Erreur',
        text: "Aucun rapport disponible",
      });
    }
  }
}
