import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

export abstract class GenericHttpService<T, ID> {
  protected constructor(protected http: HttpClient, protected base: string) {}
  get(id: ID): Observable<T> { return this.http.get<T>(`${this.base}/${id}`); }
  getAll(): Observable<T[]> { return this.http.get<T[]>(this.base); }
  save(value: Partial<T>): Observable<T> { return this.http.post<T>(this.base, value); }
  update(id: ID, value: Partial<T>): Observable<T> { return this.http.put<T>(`${this.base}/${id}`, value); }
  delete(id: ID): Observable<void> { return this.http.delete<void>(`${this.base}/${id}`); }
}
