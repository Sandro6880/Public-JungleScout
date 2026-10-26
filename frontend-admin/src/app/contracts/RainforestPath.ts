import { Checkpoint } from './Checkpoint';
export interface RainforestPath {
  id: number;
  name: string;
  pathLength: string;
  pathDuration: string;
  checkpoints: Checkpoint[];
}
